package com.shifterizator.shifterizatorbackend.openinghours.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "Special opening hours (e.g. holiday schedule) for a location on a date")
public record SpecialOpeningHoursResponseDto(
        @Schema(description = "ID") Long id,
        @Schema(description = "Location ID") Long locationId,
        @Schema(description = "Location name") String locationName,
        @Schema(description = "Date") LocalDate date,
        @Schema(description = "Open time") LocalTime openTime,
        @Schema(description = "Close time") LocalTime closeTime,
        @Schema(description = "Reason") String reason,
        @Schema(description = "Color code") String colorCode,
        @Schema(description = "Applies to company") Boolean appliesToCompany,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt,
        @Schema(description = "Created by") String createdBy,
        @Schema(description = "Updated by") String updatedBy
) {
}
