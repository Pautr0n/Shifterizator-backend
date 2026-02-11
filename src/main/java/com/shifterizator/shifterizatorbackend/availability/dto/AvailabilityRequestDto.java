package com.shifterizator.shifterizatorbackend.availability.dto;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to create or update employee availability (available, vacation, sick leave, etc.)")
public record AvailabilityRequestDto(
        @Schema(description = "Employee ID", example = "1", required = true)
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @Schema(description = "Start date (ISO)", example = "2025-02-01", required = true)
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @Schema(description = "End date (ISO)", example = "2025-02-10", required = true)
        @NotNull(message = "End date is required")
        LocalDate endDate,

        @Schema(description = "Type: AVAILABLE, VACATION, SICK_LEAVE, PERSONAL_LEAVE, UNJUSTIFIED_ABSENCE, UNAVAILABLE", required = true)
        @NotNull(message = "Availability type is required")
        AvailabilityType type
) {
}
