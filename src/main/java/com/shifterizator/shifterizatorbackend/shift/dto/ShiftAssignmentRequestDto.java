package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to assign an employee to a shift instance")
public record ShiftAssignmentRequestDto(
        @Schema(description = "Shift instance ID", example = "1", required = true)
        @NotNull(message = "Shift instance ID is required")
        Long shiftInstanceId,

        @Schema(description = "Employee ID", example = "1", required = true)
        @NotNull(message = "Employee ID is required")
        Long employeeId
) {
}
