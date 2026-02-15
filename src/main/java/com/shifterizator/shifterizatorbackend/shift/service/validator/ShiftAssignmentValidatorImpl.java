package com.shifterizator.shifterizatorbackend.shift.service.validator;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ShiftAssignmentValidatorImpl implements ShiftAssignmentValidator {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;

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
            if (type.isBlocking()) {
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
    public void validateEmployeeCompanyAndLocation(Employee employee, ShiftInstance shiftInstance) {
        var shiftLocation = shiftInstance.getLocation();
        var shiftCompany = shiftLocation.getCompany();

        boolean belongsToCompany = employee.getEmployeeCompanies().stream()
                .anyMatch(ec -> ec.getCompany().getId().equals(shiftCompany.getId()));
        if (!belongsToCompany) {
            throw new ShiftValidationException("Employee does not belong to the company for this shift");
        }

        boolean worksAtLocation = employee.getEmployeeLocations().stream()
                .anyMatch(el -> el.getLocation().getId().equals(shiftLocation.getId()));
        if (!worksAtLocation) {
            throw new ShiftValidationException("Employee is not assigned to this location");
        }
    }

    @Override
    public void validateLanguageRequirements(Employee employee, ShiftInstance shiftInstance) {
        // Intentionally no-op: do not block assignment on language. Fill with available employees.
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

        var positionReq = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .filter(stp -> stp.getPosition().getId().equals(employeePositionId))
                .findFirst()
                .orElse(null);

        if (positionReq == null || positionReq.getRequiredCount() == null || positionReq.getRequiredCount() == 0) {
            throw new ShiftValidationException("Position not required for this shift");
        }

        int requiredCount = positionReq.getRequiredCount();
        int cap = positionReq.getIdealCount() != null ? positionReq.getIdealCount() : requiredCount;

        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(
                shiftInstance.getId());
        long assignedCountForPosition = assignments.stream()
                .filter(a -> a.getEmployee().getPosition().getId().equals(employeePositionId))
                .count();

        if (assignedCountForPosition + 1 > cap) {
            throw new ShiftValidationException(
                    String.format("Position capacity reached: %d employees already assigned (max: %d)",
                            assignedCountForPosition, cap));
        }
    }

    private boolean isOverlapping(ShiftInstance shift1, ShiftInstance shift2) {
        if (!shift1.getDate().equals(shift2.getDate())) {
            return false;
        }
        return !shift1.getEndTime().isBefore(shift2.getStartTime())
                && !shift1.getStartTime().isAfter(shift2.getEndTime());
    }
}
