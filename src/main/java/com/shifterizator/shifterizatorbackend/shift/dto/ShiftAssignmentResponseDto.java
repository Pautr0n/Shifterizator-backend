package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Shift assignment (employee assigned to a shift instance) with optional warnings")
public record ShiftAssignmentResponseDto(
        @Schema(description = "Assignment ID") Long id,
        @Schema(description = "Shift instance ID") Long shiftInstanceId,
        @Schema(description = "Employee ID") Long employeeId,
        @Schema(description = "Employee display name") String employeeName,
        @Schema(description = "Whether the employee has confirmed") Boolean isConfirmed,
        @Schema(description = "When the assignment was made") LocalDateTime assignedAt,
        @Schema(description = "Username of who assigned") String assignedBy,
        @Schema(description = "When the employee confirmed") LocalDateTime confirmedAt,
        @Schema(description = "Preference-related warnings (e.g. assigned on preferred day off); never null")
        List<String> warnings
) {
}
