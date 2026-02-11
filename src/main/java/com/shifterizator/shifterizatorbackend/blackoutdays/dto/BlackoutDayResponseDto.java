package com.shifterizator.shifterizatorbackend.blackoutdays.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Blackout day (location closed or no shifts on a date)")
public record BlackoutDayResponseDto(
        @Schema(description = "Blackout day ID") Long id,
        @Schema(description = "Location ID") Long locationId,
        @Schema(description = "Location name") String locationName,
        @Schema(description = "Date") LocalDate date,
        @Schema(description = "Reason") String reason,
        @Schema(description = "Applies to company") Boolean appliesToCompany,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt,
        @Schema(description = "Created by") String createdBy,
        @Schema(description = "Updated by") String updatedBy
) {
}
