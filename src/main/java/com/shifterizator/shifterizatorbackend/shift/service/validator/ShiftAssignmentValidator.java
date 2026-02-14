package com.shifterizator.shifterizatorbackend.shift.service.validator;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;

public interface ShiftAssignmentValidator {

    void validateNotAlreadyAssigned(Long shiftInstanceId, Long employeeId);

    void validateEmployeeAvailability(Long employeeId, java.time.LocalDate date);

    void validatePositionMatch(Employee employee, ShiftInstance shiftInstance);

    void validateEmployeeCompanyAndLocation(Employee employee, ShiftInstance shiftInstance);

    void validateLanguageRequirements(Employee employee, ShiftInstance shiftInstance);

    void validateNoOverlappingShifts(Long employeeId, ShiftInstance shiftInstance);

    void validatePositionCapacity(Employee employee, ShiftInstance shiftInstance);
}
