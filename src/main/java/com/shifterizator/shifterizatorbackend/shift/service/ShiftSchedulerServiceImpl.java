package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.advisor.ShiftCandidateTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftSchedulerServiceImpl implements ShiftSchedulerService {


    private final ShiftInstanceRepository shiftInstanceRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftCandidateTierService shiftCandidateTierService;

    @Override
    public void scheduleDay(Long locationId, LocalDate date) {
        List<ShiftInstance> instances = loadAndSortInstancesByPriority(locationId, date);
        if (instances.isEmpty()) {
            return;
        }

        List<Employee> candidates = loadCandidatesForDay(locationId, date);
        if (candidates.isEmpty()) {
            log.debug("No candidates for location {} on {}", locationId, date);
            return;
        }

        fillMinimumsForAllShifts(instances, candidates, date, locationId);
        fillUpToIdealByPriority(instances, candidates, date, locationId);
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
     * Instances for the day, ordered by template priority (lower = higher priority) then start time.
     * Null priority is treated as lowest (sorted last).
     */
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

    /**
     * Candidates at the location, available on the date, with shift preferences loaded for tier computation.
     */
    private List<Employee> loadCandidatesForDay(Long locationId, LocalDate date) {
        List<Employee> withPreferences = employeeRepository.findActiveByLocationIdWithShiftPreferences(locationId);
        List<Employee> available = withPreferences.stream()
                .filter(e -> isAvailableOnDate(e.getId(), date))
                .toList();
        return available;
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

    /** Candidates sorted by tier for the given shift (best first). */
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

    /** Phase 1: Fill every shift up to requiredEmployees using tier-ordered candidates. Higher-priority shifts processed first. */
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

    /** Phase 2: Fill up to ideal per shift, higher-priority shifts first. */
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

    /** Phase 4: Fill remaining slots with language-qualified employees, spread across shifts (tier-ordered). */
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
