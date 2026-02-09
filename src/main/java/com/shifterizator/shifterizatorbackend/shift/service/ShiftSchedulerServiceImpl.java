package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftSchedulerServiceImpl implements ShiftSchedulerService {

    private static final Set<AvailabilityType> BLOCKING_AVAILABILITY_TYPES = Set.of(
            AvailabilityType.VACATION,
            AvailabilityType.SICK_LEAVE,
            AvailabilityType.UNAVAILABLE,
            AvailabilityType.UNJUSTIFIED_ABSENCE,
            AvailabilityType.PERSONAL_LEAVE
    );

    private static final String MANAGER_POSITION_NAME = "manager";

    private final ShiftInstanceRepository shiftInstanceRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftAssignmentService shiftAssignmentService;

    @Override
    public void scheduleDay(Long locationId, LocalDate date) {
        List<ShiftInstance> instances = loadShiftInstancesForDay(locationId, date);
        if (instances.isEmpty()) {
            return;
        }

        List<Employee> candidates = loadCandidatesForDay(locationId, date);
        if (candidates.isEmpty()) {
            log.debug("No candidates for location {} on {}", locationId, date);
            return;
        }

        fillMinimumsForAllShifts(instances, candidates, date, locationId);
        fillAfternoonShiftsFirst(instances, candidates, date, locationId);
        ensureManagerPresent(instances, candidates, date, locationId);
        improveLanguageDistribution(instances, candidates, date, locationId);
    }

    @Override
    public void scheduleMonth(Long locationId, YearMonth yearMonth) {
        LocalDate first = yearMonth.atDay(1);
        LocalDate last = yearMonth.atEndOfMonth();
        for (LocalDate date = first; !date.isAfter(last); date = date.plusDays(1)) {
            scheduleDay(locationId, date);
        }
    }

    /**
     * Shift instances for the location and date, ordered by start time (morning first).
     */
    private List<ShiftInstance> loadShiftInstancesForDay(Long locationId, LocalDate date) {
        return shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(locationId, date);
    }

    /**
     * Employees at the location who are available on the date (no blocking availability).
     */
    private List<Employee> loadCandidatesForDay(Long locationId, LocalDate date) {
        List<Employee> atLocation = employeeRepository.findActiveByLocationId(locationId);
        return atLocation.stream()
                .filter(employee -> isAvailableOnDate(employee.getId(), date))
                .toList();
    }

    private boolean isAvailableOnDate(Long employeeId, LocalDate date) {
        List<EmployeeAvailability> overlapping = employeeAvailabilityRepository.findOverlapping(
                employeeId, date, date, null);
        return overlapping.stream()
                .noneMatch(ea -> BLOCKING_AVAILABILITY_TYPES.contains(ea.getType()));
    }

    private Set<Long> getAssignedEmployeeIdsForDay(List<ShiftInstance> instances) {
        if (instances.isEmpty()) {
            return Set.of();
        }
        List<Long> instanceIds = instances.stream().map(ShiftInstance::getId).toList();
        return shiftAssignmentRepository.findAssignedEmployeeIdsByShiftInstanceIdIn(instanceIds)
                .stream()
                .collect(Collectors.toSet());
    }

    private List<Employee> unassignedCandidates(List<Employee> candidates, Set<Long> assignedIds) {
        return candidates.stream()
                .filter(e -> !assignedIds.contains(e.getId()))
                .toList();
    }

    private int getIdealTarget(ShiftInstance instance) {
        return instance.getIdealEmployees() != null ? instance.getIdealEmployees() : instance.getRequiredEmployees();
    }

    private boolean shiftNeedsMore(ShiftInstance instance, int targetCount) {
        int current = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(instance.getId()).size();
        return current < targetCount;
    }

    /**
     * Attempts to assign the employee to the shift. Returns true if assigned, false if validation failed.
     */
    private boolean tryAssign(Long shiftInstanceId, Long employeeId) {
        try {
            shiftAssignmentService.assign(new ShiftAssignmentRequestDto(shiftInstanceId, employeeId));
            return true;
        } catch (ShiftValidationException e) {
            log.trace("Assignment skipped: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Priority 0: Fill every shift up to its required minimum (requiredEmployees).
     */
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

        while (shiftNeedsMore(instance, targetCount) && !available.isEmpty()) {
            boolean assignedSomeone = false;
            for (Employee employee : available) {
                if (tryAssign(instance.getId(), employee.getId())) {
                    assignedSomeone = true;
                    assigned.add(employee.getId());
                    available = unassignedCandidates(candidates, assigned);
                    break;
                }
            }
            if (!assignedSomeone) {
                break;
            }
        }
    }

    /**
     * Fill toward ideal per shift (idealEmployees or required if null), afternoon shifts first.
     */
    private void fillAfternoonShiftsFirst(List<ShiftInstance> instances, List<Employee> candidates,
                                         LocalDate date, Long locationId) {
        List<ShiftInstance> afternoonFirst = instances.stream()
                .sorted(Comparator.comparing(ShiftInstance::getStartTime).reversed())
                .toList();

        for (ShiftInstance instance : afternoonFirst) {
            int target = getIdealTarget(instance);
            if (!shiftNeedsMore(instance, target)) {
                continue;
            }
            fillShiftUpTo(instance, target, candidates, date, locationId, instances);
        }
    }

    /**
     * Priority 3: Ensure at least one manager is present during the day, preferably in the afternoon.
     */
    private void ensureManagerPresent(List<ShiftInstance> instances, List<Employee> candidates,
                                      LocalDate date, Long locationId) {
        Set<Long> assigned = getAssignedEmployeeIdsForDay(instances);
        boolean managerAlreadyAssigned = candidates.stream()
                .filter(e -> assigned.contains(e.getId()))
                .anyMatch(this::isManager);

        if (managerAlreadyAssigned) {
            return;
        }

        List<Employee> managerCandidates = unassignedCandidates(candidates, assigned).stream()
                .filter(this::isManager)
                .toList();

        if (managerCandidates.isEmpty()) {
            log.debug("No manager available for location {} on {}", locationId, date);
            return;
        }

        List<ShiftInstance> afternoonFirst = instances.stream()
                .sorted(Comparator.comparing(ShiftInstance::getStartTime).reversed())
                .toList();

        for (ShiftInstance instance : afternoonFirst) {
            for (Employee manager : managerCandidates) {
                if (tryAssign(instance.getId(), manager.getId())) {
                    return;
                }
            }
        }
    }

    private boolean isManager(Employee employee) {
        return employee.getPosition() != null
                && MANAGER_POSITION_NAME.equalsIgnoreCase(employee.getPosition().getName());
    }

    /**
     * Priority 4: Try to assign employees who meet language requirements and spread them across shifts.
     */
    private void improveLanguageDistribution(List<ShiftInstance> instances, List<Employee> candidates,
                                             LocalDate date, Long locationId) {
        Set<Long> assigned = getAssignedEmployeeIdsForDay(instances);
        List<Employee> available = unassignedCandidates(candidates, assigned);

        for (ShiftInstance instance : instances) {
            if (!shiftNeedsMore(instance, getIdealTarget(instance))) {
                continue;
            }
            for (Employee employee : available) {
                if (tryAssign(instance.getId(), employee.getId())) {
                    assigned.add(employee.getId());
                    available = unassignedCandidates(candidates, assigned);
                    break;
                }
            }
        }
    }
}
