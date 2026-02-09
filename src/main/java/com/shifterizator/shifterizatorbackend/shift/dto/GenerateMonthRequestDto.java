package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for generating shift instances for a location and month.
 */
public record GenerateMonthRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,
        @NotNull(message = "Year is required")
        @Min(2020)
        @Max(2100)
        Integer year,
        @NotNull(message = "Month is required")
        @Min(1)
        @Max(12)
        Integer month
) {
}
