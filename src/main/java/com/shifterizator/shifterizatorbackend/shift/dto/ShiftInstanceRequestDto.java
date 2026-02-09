package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftInstanceRequestDto(
        @NotNull(message = "Shift template ID is required")
        Long shiftTemplateId,

        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Required employees is required")
        @Positive(message = "Required employees must be positive")
        Integer requiredEmployees,

        @Size(max = 200, message = "Notes must not exceed 200 characters")
        String notes
) {
}
