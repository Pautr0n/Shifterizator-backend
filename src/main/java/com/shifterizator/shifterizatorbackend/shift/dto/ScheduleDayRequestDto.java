package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request body for triggering auto-assignment for a location on a single date.
 */
public record ScheduleDayRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,
        @NotNull(message = "Date is required")
        LocalDate date
) {
}
