package com.shifterizator.shifterizatorbackend.shift.service.validator;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;

/**
 * Validator interface for shift assignment business rules.
 */
public interface ShiftAssignmentValidator {

    /**
     * Validates that the employee is not already assigned to the shift.
     *
     * @param shiftInstanceId the shift instance ID
     * @param employeeId the employee ID
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if already assigned
     */
    void validateNotAlreadyAssigned(Long shiftInstanceId, Long employeeId);

    /**
     * Validates that the employee is available (not on vacation, sick leave, etc.).
     *
     * @param employeeId the employee ID
     * @param date the date to check
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if unavailable
     */
    void validateEmployeeAvailability(Long employeeId, java.time.LocalDate date);

    /**
     * Validates that the employee's position matches the shift requirements.
     *
     * @param employee the employee
     * @param shiftInstance the shift instance
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if position doesn't match
     */
    void validatePositionMatch(Employee employee, ShiftInstance shiftInstance);

    /**
     * Validates that the employee belongs to the company and works at the location
     * of the given shift instance.
     *
     * @param employee the employee
     * @param shiftInstance the shift instance
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException
     *         if the employee is not linked to the company or location
     */
    void validateEmployeeCompanyAndLocation(Employee employee, ShiftInstance shiftInstance);

    /**
     * Validates that the employee meets language requirements for the shift.
     *
     * @param employee the employee
     * @param shiftInstance the shift instance
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if language requirements not met
     */
    void validateLanguageRequirements(Employee employee, ShiftInstance shiftInstance);

    /**
     * Validates that the employee doesn't have overlapping shifts.
     *
     * @param employeeId the employee ID
     * @param shiftInstance the shift instance to check
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if overlapping shift exists
     */
    void validateNoOverlappingShifts(Long employeeId, ShiftInstance shiftInstance);

    /**
     * Validates that the position capacity hasn't been reached.
     *
     * @param employee the employee
     * @param shiftInstance the shift instance
     * @throws com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException if capacity reached
     */
    void validatePositionCapacity(Employee employee, ShiftInstance shiftInstance);
}
