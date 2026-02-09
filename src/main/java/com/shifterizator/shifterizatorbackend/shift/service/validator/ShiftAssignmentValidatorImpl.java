package com.shifterizator.shifterizatorbackend.shift.service.validator;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeLanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShiftAssignmentValidatorImpl implements ShiftAssignmentValidator {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final EmployeeLanguageRepository employeeLanguageRepository;

    @Override
    public void validateNotAlreadyAssigned(Long shiftInstanceId, Long employeeId) {
        if (shiftAssignmentRepository.findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(
                shiftInstanceId, employeeId).isPresent()) {
            throw new ShiftValidationException("Employee is already assigned to this shift");
        }
    }

    @Override
    public void validateEmployeeAvailability(Long employeeId, java.time.LocalDate date) {
        List<EmployeeAvailability> availabilities = employeeAvailabilityRepository.findOverlapping(
                employeeId, date, date, null);

        for (EmployeeAvailability availability : availabilities) {
            AvailabilityType type = availability.getType();
            if (type == AvailabilityType.VACATION
                    || type == AvailabilityType.SICK_LEAVE
                    || type == AvailabilityType.UNAVAILABLE
                    || type == AvailabilityType.UNJUSTIFIED_ABSENCE) {
                throw new ShiftValidationException(
                        String.format("Employee is marked as %s on this date", type.name()));
            }
        }
    }

    @Override
    public void validatePositionMatch(Employee employee, ShiftInstance shiftInstance) {
        Long employeePositionId = employee.getPosition().getId();
        boolean positionMatches = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .anyMatch(stp -> stp.getPosition().getId().equals(employeePositionId));

        if (!positionMatches) {
            throw new ShiftValidationException("Employee position does not match any required position for this shift template");
        }
    }

    @Override
    public void validateLanguageRequirements(Employee employee, ShiftInstance shiftInstance) {
        Set<Long> requiredLanguageIds = shiftInstance.getShiftTemplate().getRequiredLanguages().stream()
                .map(lang -> lang.getId())
                .collect(Collectors.toSet());

        if (requiredLanguageIds.isEmpty()) {
            return; // No language requirements
        }

        List<EmployeeLanguage> employeeLanguages = employeeLanguageRepository.findByEmployee_Id(employee.getId());
        Set<Long> employeeLanguageIds = employeeLanguages.stream()
                .map(el -> el.getLanguage().getId())
                .collect(Collectors.toSet());

        if (!employeeLanguageIds.containsAll(requiredLanguageIds)) {
            throw new ShiftValidationException("Employee does not meet language requirements for this shift");
        }
    }

    @Override
    public void validateNoOverlappingShifts(Long employeeId, ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeAndDate(
                employeeId, shiftInstance.getDate());

        for (ShiftAssignment assignment : assignments) {
            ShiftInstance existingShift = assignment.getShiftInstance();
            if (isOverlapping(shiftInstance, existingShift)) {
                throw new ShiftValidationException("Employee already has an overlapping shift assignment");
            }
        }
    }

    @Override
    public void validatePositionCapacity(Employee employee, ShiftInstance shiftInstance) {
        Long employeePositionId = employee.getPosition().getId();

        // Find required count for this position
        Integer requiredCount = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .filter(stp -> stp.getPosition().getId().equals(employeePositionId))
                .map(stp -> stp.getRequiredCount())
                .findFirst()
                .orElse(0);

        if (requiredCount == 0) {
            throw new ShiftValidationException("Position not required for this shift");
        }

        // Count how many employees of this position are already assigned
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(
                shiftInstance.getId());
        long assignedCountForPosition = assignments.stream()
                .filter(a -> a.getEmployee().getPosition().getId().equals(employeePositionId))
                .count();

        // Check if adding this employee would exceed or reach capacity
        // We check if current count + 1 would exceed or equal requiredCount
        if (assignedCountForPosition + 1 >= requiredCount) {
            throw new ShiftValidationException(
                    String.format("Position capacity reached: %d employees already assigned (required: %d)",
                            assignedCountForPosition, requiredCount));
        }
    }

    /**
     * Checks if two shift instances overlap in time.
     *
     * @param shift1 the first shift instance
     * @param shift2 the second shift instance
     * @return true if shifts overlap, false otherwise
     */
    private boolean isOverlapping(ShiftInstance shift1, ShiftInstance shift2) {
        if (!shift1.getDate().equals(shift2.getDate())) {
            return false;
        }
        // Check if time ranges overlap
        return !shift1.getEndTime().isBefore(shift2.getStartTime())
                && !shift1.getStartTime().isAfter(shift2.getEndTime());
    }
}
