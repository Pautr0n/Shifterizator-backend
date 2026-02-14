package com.shifterizator.shifterizatorbackend.shift.dto;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;

import java.util.List;

public record ShiftAssignmentAssignResult(
        ShiftAssignment assignment,
        List<String> warnings
) {
}
