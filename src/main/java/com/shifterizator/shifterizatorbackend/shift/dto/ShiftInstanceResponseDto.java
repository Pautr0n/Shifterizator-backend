package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Schema(description = "Shift instance (one shift slot on a date at a location) with assignment count")
public record ShiftInstanceResponseDto(
        @Schema(description = "Shift instance ID") Long id,
        @Schema(description = "Shift template ID") Long shiftTemplateId,
        @Schema(description = "Location ID") Long locationId,
        @Schema(description = "Location name") String locationName,
        @Schema(description = "Date") LocalDate date,
        @Schema(description = "Start time") LocalTime startTime,
        @Schema(description = "End time") LocalTime endTime,
        @Schema(description = "Required employees") Integer requiredEmployees,
        @Schema(description = "Ideal employees") Integer idealEmployees,
        @Schema(description = "Number of assigned employees") Integer assignedEmployees,
        @Schema(description = "Whether staffing is complete") Boolean isComplete,
        @Schema(description = "Notes") String notes,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt,
        @Schema(description = "Created by username") String createdBy,
        @Schema(description = "Updated by username") String updatedBy
) {
}
