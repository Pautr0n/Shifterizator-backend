package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to trigger auto-assignment for a location on a single date")
public record ScheduleDayRequestDto(
        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,
        @Schema(description = "Date (ISO)", example = "2025-02-10", required = true)
        @NotNull(message = "Date is required")
        LocalDate date
) {
}
