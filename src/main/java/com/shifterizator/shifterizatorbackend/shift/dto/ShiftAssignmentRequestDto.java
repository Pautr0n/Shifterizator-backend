package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.NotNull;

public record ShiftAssignmentRequestDto(
        @NotNull(message = "Shift instance ID is required")
        Long shiftInstanceId,

        @NotNull(message = "Employee ID is required")
        Long employeeId
) {
}
