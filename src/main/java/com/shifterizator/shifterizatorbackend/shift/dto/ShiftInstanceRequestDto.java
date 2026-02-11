package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Request to create or update a shift instance (one shift slot on a date at a location)")
public record ShiftInstanceRequestDto(
        @Schema(description = "Shift template ID", example = "1", required = true)
        @NotNull(message = "Shift template ID is required")
        Long shiftTemplateId,

        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,

        @Schema(description = "Date (ISO)", example = "2025-02-10", required = true)
        @NotNull(message = "Date is required")
        LocalDate date,

        @Schema(description = "Shift start time", example = "09:00", required = true)
        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @Schema(description = "Shift end time", example = "17:00", required = true)
        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Schema(description = "Minimum required employees", example = "2", required = true)
        @NotNull(message = "Required employees is required")
        @Positive(message = "Required employees must be positive")
        Integer requiredEmployees,

        @Schema(description = "Ideal headcount when enough staff; must be >= requiredEmployees if set")
        Integer idealEmployees,

        @Schema(description = "Optional notes (max 200 characters)")
        @Size(max = 200, message = "Notes must not exceed 200 characters")
        String notes
) {
}
