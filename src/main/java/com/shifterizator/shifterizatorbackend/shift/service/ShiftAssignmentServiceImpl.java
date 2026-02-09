package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeLanguageRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAssignmentNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final EmployeeLanguageRepository employeeLanguageRepository;

    @Override
    public ShiftAssignment assign(ShiftAssignmentRequestDto dto) {
        ShiftInstance shiftInstance = shiftInstanceRepository.findById(dto.shiftInstanceId())
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftInstanceNotFoundException("Shift instance not found"));

        Employee employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        // Check if already assigned
        if (shiftAssignmentRepository.findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(
                dto.shiftInstanceId(), dto.employeeId()).isPresent()) {
            throw new ShiftValidationException("Employee is already assigned to this shift");
        }

        // Validate employee availability
        validateEmployeeAvailability(employee.getId(), shiftInstance.getDate());

        // Validate position match
        validatePositionMatch(employee, shiftInstance);

        // Validate language requirements
        validateLanguageRequirements(employee, shiftInstance);

        // Check for overlapping shifts
        validateNoOverlappingShifts(employee.getId(), shiftInstance);

        // Check capacity per position
        validatePositionCapacity(employee, shiftInstance);

        ShiftAssignment assignment = ShiftAssignment.builder()
                .shiftInstance(shiftInstance)
                .employee(employee)
                .isConfirmed(false)
                .build();

        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);

        // Update shift instance completeness
        updateShiftInstanceCompleteness(shiftInstance);

        return saved;
    }

    @Override
    public void unassign(Long shiftInstanceId, Long employeeId) {
        ShiftAssignment assignment = shiftAssignmentRepository
                .findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(shiftInstanceId, employeeId)
                .orElseThrow(() -> new ShiftAssignmentNotFoundException("Assignment not found"));

        assignment.setDeletedAt(LocalDateTime.now());

        ShiftInstance shiftInstance = assignment.getShiftInstance();
        updateShiftInstanceCompleteness(shiftInstance);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftAssignment findById(Long id) {
        return shiftAssignmentRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftAssignmentNotFoundException("Assignment not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByShiftInstance(Long shiftInstanceId) {
        return shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(shiftInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByEmployee(Long employeeId) {
        return shiftAssignmentRepository
                .findByEmployee_IdAndDeletedAtIsNullOrderByShiftInstance_DateAscShiftInstance_StartTimeAsc(employeeId);
    }

    private void validateEmployeeAvailability(Long employeeId, java.time.LocalDate date) {
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

    private void validatePositionMatch(Employee employee, ShiftInstance shiftInstance) {
        Long employeePositionId = employee.getPosition().getId();
        boolean positionMatches = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .anyMatch(stp -> stp.getPosition().getId().equals(employeePositionId));
        
        if (!positionMatches) {
            throw new ShiftValidationException("Employee position does not match any required position for this shift template");
        }
    }

    private void validateLanguageRequirements(Employee employee, ShiftInstance shiftInstance) {
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

    private void validateNoOverlappingShifts(Long employeeId, ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeAndDate(
                employeeId, shiftInstance.getDate());

        for (ShiftAssignment assignment : assignments) {
            ShiftInstance existingShift = assignment.getShiftInstance();
            if (isOverlapping(shiftInstance, existingShift)) {
                throw new ShiftValidationException("Employee already has an overlapping shift assignment");
            }
        }
    }

    private boolean isOverlapping(ShiftInstance shift1, ShiftInstance shift2) {
        if (!shift1.getDate().equals(shift2.getDate())) {
            return false;
        }
        // Check if time ranges overlap
        return !shift1.getEndTime().isBefore(shift2.getStartTime())
                && !shift1.getStartTime().isAfter(shift2.getEndTime());
    }

    private void validatePositionCapacity(Employee employee, ShiftInstance shiftInstance) {
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
        
        if (assignedCountForPosition >= requiredCount) {
            throw new ShiftValidationException(
                    String.format("Position capacity reached: %d employees already assigned (required: %d)",
                            assignedCountForPosition, requiredCount));
        }
    }

    private void updateShiftInstanceCompleteness(ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(
                shiftInstance.getId());
        
        // Check if all position requirements are met
        boolean isComplete = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .allMatch(stp -> {
                    long assignedCount = assignments.stream()
                            .filter(a -> a.getEmployee().getPosition().getId().equals(stp.getPosition().getId()))
                            .count();
                    return assignedCount >= stp.getRequiredCount();
                });
        
        shiftInstance.setIsComplete(isComplete);
    }
}
