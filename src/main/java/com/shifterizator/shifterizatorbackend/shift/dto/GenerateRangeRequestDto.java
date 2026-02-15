package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Request to generate shift instances for a location and date range (Monday to Sunday, max 8 weeks)")
public record GenerateRangeRequestDto(
        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,
        @Schema(description = "Start date (must be Monday)", example = "2025-02-03", required = true)
        @NotNull(message = "Start date is required")
        LocalDate startDate,
        @Schema(description = "End date (must be Sunday)", example = "2025-02-09", required = true)
        @NotNull(message = "End date is required")
        LocalDate endDate,
        @Schema(description = "If true, replace existing shifts on those dates; if false, return 409 when any date has existing shifts")
        Boolean replaceExisting
) {
}
