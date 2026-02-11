package com.shifterizator.shifterizatorbackend.blackoutdays.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request to create or update a blackout day (location closed or no shifts)")
public record BlackoutDayRequestDto(
        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,

        @Schema(description = "Date (ISO)", example = "2025-02-10", required = true)
        @NotNull(message = "Date is required")
        LocalDate date,

        @Schema(description = "Reason (max 200 chars)", example = "Public holiday", required = true)
        @NotNull(message = "Reason is required")
        @Size(max = 200, message = "Reason must not exceed 200 characters")
        String reason,

        @Schema(description = "Whether this applies to all locations of the company", required = true)
        @NotNull(message = "Applies to company flag is required")
        Boolean appliesToCompany
) {
}
