package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentAssignResult;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;

import java.time.LocalDate;
import java.util.List;

public interface ShiftAssignmentService {

    ShiftAssignmentAssignResult assign(ShiftAssignmentRequestDto dto);

    void unassign(Long shiftInstanceId, Long employeeId);

    /**
     * Unassigns the employee from all shifts in the given date range (e.g. when blocking availability is created/updated).
     * Each affected shift instance's completeness is updated after unassign.
     */
    void unassignEmployeeFromShiftsInDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

    ShiftAssignment findById(Long id);

    List<ShiftAssignment> findByShiftInstance(Long shiftInstanceId);

    List<ShiftAssignment> findByEmployee(Long employeeId);
}
