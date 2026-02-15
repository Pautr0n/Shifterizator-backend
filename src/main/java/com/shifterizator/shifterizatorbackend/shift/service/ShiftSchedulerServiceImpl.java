package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import com.shifterizator.shifterizatorbackend.company.service.domain.WeekBounds;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ScheduleDaySkippedException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.advisor.ShiftCandidateTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftSchedulerServiceImpl implements ShiftSchedulerService {

    private static final int DEFAULT_SHIFTS_PER_WEEK = 5;
    private static final int MAX_RANGE_DAYS = 56;

    private final ShiftInstanceRepository shiftInstanceRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftCandidateTierService shiftCandidateTierService;
    private final LocationService locationService;

    @Lazy
    @Autowired
    private ScheduleDayRunner scheduleDayRunner;

    @Override
    public void scheduleDay(Long locationId, LocalDate date) {
        Location location = locationService.findById(locationId);

        List<ShiftInstance> instances = loadAndSortInstancesByPriority(locationId, date);
        if (instances.isEmpty()) {
            throw new ScheduleDaySkippedException("No shifts defined for this day.");
        }

        List<Employee> candidates = loadCandidatesForDay(locationId, date);
        if (candidates.isEmpty()) {
            throw new ScheduleDaySkippedException("No candidates available on: " + date);
        }

        List<Employee> candidatesWithinCap = filterByMaxShiftsPerWeek(
                candidates, date, location.getFirstDayOfWeek());
        if (candidatesWithinCap.isEmpty()) {
            throw new ScheduleDaySkippedException("No candidates under 5 shifts/week available on: " + date);
        }

        fillMinimumsForAllShifts(instances, candidatesWithinCap, date, locationId);
        fillUpToIdealByPriority(instances, candidatesWithinCap, date, locationId);
        improveLanguageDistribution(instances, candidatesWithinCap, date, locationId);
    }

    @Override
    public void scheduleRange(Long locationId, LocalDate startDate, LocalDate endDate) {
        validateRange(startDate, endDate);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            try {
                scheduleDayRunner.runScheduleDay(locationId, date);
            } catch (ScheduleDaySkippedException e) {
                log.debug("Skipped scheduling for {}: {}", date, e.getMessage());
            }
        }
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new ShiftValidationException("Start date must be a Monday");
        }
        if (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new ShiftValidationException("End date must be a Sunday");
        }
        if (endDate.isBefore(startDate)) {
            throw new ShiftValidationException("End date must be on or after start date");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new ShiftValidationException("Range must not exceed 8 weeks");
        }
    }

    private List<ShiftInstance> loadAndSortInstancesByPriority(Long locationId, LocalDate date) {
        List<ShiftInstance> list = shiftInstanceRepository
                .findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(locationId, date);
        return list.stream()
                .sorted(Comparator
                        .comparing(ShiftInstance::getShiftTemplate,
                                Comparator.nullsLast(Comparator.comparing(ShiftTemplate::getPriority, Comparator.nullsLast(Integer::compareTo))))
                        .thenComparing(ShiftInstance::getStartTime))
                .toList();
    }

    private List<Employee> loadCandidatesForDay(Long locationId, LocalDate date) {
        List<Employee> withPreferences = employeeRepository.findActiveByLocationIdWithShiftPreferences(locationId);
        List<Employee> available = withPreferences.stream()
                .filter(e -> isAvailableOnDate(e.getId(), date))
                .toList();
        return available;
    }

    private List<Employee> filterByMaxShiftsPerWeek(
            List<Employee> candidates,
            LocalDate date,
            DayOfWeek firstDayOfWeek) {
        LocalDate weekStart = WeekBounds.weekStart(date, firstDayOfWeek);
        LocalDate weekEnd = WeekBounds.weekEnd(date, firstDayOfWeek);

        return candidates.stream()
                .filter(employee -> {
                    int maxAllowed = employee.getShiftsPerWeek() != null
                            ? employee.getShiftsPerWeek()
                            : DEFAULT_SHIFTS_PER_WEEK;
                    long count = shiftAssignmentRepository
                            .countByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(
                                    employee.getId(), weekStart, weekEnd);
                    return count < maxAllowed;
                })
                .toList();
    }

    private boolean isAvailableOnDate(Long employeeId, LocalDate date) {
        List<EmployeeAvailability> overlapping = employeeAvailabilityRepository.findOverlapping(
                employeeId, date, date, null);
        return overlapping.stream()
                .noneMatch(ea -> ea.getType().isBlocking());
    }

    private Set<Long> getAssignedEmployeeIdsForDay(List<ShiftInstance> instances) {
        if (instances.isEmpty()) {
            return Set.of();
        }
        List<Long> instanceIds = instances.stream().map(ShiftInstance::getId).toList();
        return new HashSet<>(shiftAssignmentRepository.findAssignedEmployeeIdsByShiftInstanceIdIn(instanceIds));
    }

    private List<Employee> unassignedCandidates(List<Employee> candidates, Set<Long> assignedIds) {
        return candidates.stream()
                .filter(e -> !assignedIds.contains(e.getId()))
                .toList();
    }

    private List<Employee> candidatesByTier(List<Employee> candidates, ShiftInstance instance, LocalDate date) {
        return candidates.stream()
                .sorted(Comparator.comparingInt(e -> shiftCandidateTierService.getTier(e, instance, date)))
                .toList();
    }

    private int getIdealTarget(ShiftInstance instance) {
        return instance.getIdealEmployees() != null ? instance.getIdealEmployees() : instance.getRequiredEmployees();
    }

    private boolean shiftNeedsMore(ShiftInstance instance, int targetCount) {
        int current = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(instance.getId()).size();
        return current < targetCount;
    }

    private boolean tryAssign(Long shiftInstanceId, Long employeeId) {
        try {
            shiftAssignmentService.assign(new ShiftAssignmentRequestDto(shiftInstanceId, employeeId));
            return true;
        } catch (ShiftValidationException e) {
            log.trace("Assignment skipped: {}", e.getMessage());
            return false;
        }
    }

    private void fillMinimumsForAllShifts(List<ShiftInstance> instances, List<Employee> candidates,
                                          LocalDate date, Long locationId) {
        for (ShiftInstance instance : instances) {
            fillShiftUpTo(instance, instance.getRequiredEmployees(), candidates, date, locationId, instances);
        }
    }

    private void fillShiftUpTo(ShiftInstance instance, int targetCount, List<Employee> candidates,
                               LocalDate date, Long locationId, List<ShiftInstance> allInstances) {
        Set<Long> assigned = getAssignedEmployeeIdsForDay(allInstances);
        List<Employee> available = unassignedCandidates(candidates, assigned);
        List<Employee> byTier = candidatesByTier(available, instance, date);

        while (shiftNeedsMore(instance, targetCount) && !byTier.isEmpty()) {
            boolean assignedSomeone = false;
            for (Employee employee : byTier) {
                if (assigned.contains(employee.getId())) {
                    continue;
                }
                if (tryAssign(instance.getId(), employee.getId())) {
                    assignedSomeone = true;
                    assigned.add(employee.getId());
                    available = unassignedCandidates(candidates, assigned);
                    byTier = candidatesByTier(available, instance, date);
                    break;
                }
            }
            if (!assignedSomeone) {
                break;
            }
        }
    }

    private void fillUpToIdealByPriority(List<ShiftInstance> instances, List<Employee> candidates,
                                         LocalDate date, Long locationId) {
        for (ShiftInstance instance : instances) {
            int target = getIdealTarget(instance);
            if (!shiftNeedsMore(instance, target)) {
                continue;
            }
            fillShiftUpTo(instance, target, candidates, date, locationId, instances);
        }
    }

    private void improveLanguageDistribution(List<ShiftInstance> instances, List<Employee> candidates,
                                             LocalDate date, Long locationId) {
        Set<Long> assigned = getAssignedEmployeeIdsForDay(instances);
        List<Employee> available = unassignedCandidates(candidates, assigned);

        for (ShiftInstance instance : instances) {
            if (!shiftNeedsMore(instance, getIdealTarget(instance))) {
                continue;
            }
            List<Employee> byTier = candidatesByTier(available, instance, date);
            for (Employee employee : byTier) {
                if (tryAssign(instance.getId(), employee.getId())) {
                    assigned.add(employee.getId());
                    available = unassignedCandidates(candidates, assigned);
                    break;
                }
            }
        }
    }
}
