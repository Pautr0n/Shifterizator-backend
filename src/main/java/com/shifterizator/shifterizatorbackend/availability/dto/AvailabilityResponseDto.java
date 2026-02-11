package com.shifterizator.shifterizatorbackend.availability.dto;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Employee availability entry (available, vacation, sick leave, etc.)")
public record AvailabilityResponseDto(
        @Schema(description = "Availability ID") Long id,
        @Schema(description = "Employee ID") Long employeeId,
        @Schema(description = "Employee display name") String employeeName,
        @Schema(description = "Start date") LocalDate startDate,
        @Schema(description = "End date") LocalDate endDate,
        @Schema(description = "Availability type") AvailabilityType type,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt,
        @Schema(description = "Created by username") String createdBy,
        @Schema(description = "Updated by username") String updatedBy
) {
}
