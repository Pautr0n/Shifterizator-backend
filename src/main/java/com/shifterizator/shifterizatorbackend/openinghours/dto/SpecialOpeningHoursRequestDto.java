package com.shifterizator.shifterizatorbackend.openinghours.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Request to create or update special opening hours (e.g. holiday schedule)")
public record SpecialOpeningHoursRequestDto(
        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,

        @Schema(description = "Date (ISO)", example = "2025-02-10", required = true)
        @NotNull(message = "Date is required")
        LocalDate date,

        @Schema(description = "Open time", example = "10:00", required = true)
        @NotNull(message = "Open time is required")
        LocalTime openTime,

        @Schema(description = "Close time", example = "18:00", required = true)
        @NotNull(message = "Close time is required")
        LocalTime closeTime,

        @Schema(description = "Reason (max 200 chars)", example = "Holiday hours", required = true)
        @NotNull(message = "Reason is required")
        @Size(max = 200, message = "Reason must not exceed 200 characters")
        String reason,

        @Schema(description = "Optional color code (max 20 chars)")
        @Size(max = 20, message = "Color code must not exceed 20 characters")
        String colorCode,

        @Schema(description = "Whether this applies to all locations of the company", required = true)
        @NotNull(message = "Applies to company flag is required")
        Boolean appliesToCompany
) {
}
