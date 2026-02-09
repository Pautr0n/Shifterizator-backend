package com.shifterizator.shifterizatorbackend.shift.dto;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;

import java.util.List;

/**
 * Result of an assign operation, including the assignment and any preference-related warnings.
 */
public record ShiftAssignmentAssignResult(
        ShiftAssignment assignment,
        List<String> warnings
) {
}
