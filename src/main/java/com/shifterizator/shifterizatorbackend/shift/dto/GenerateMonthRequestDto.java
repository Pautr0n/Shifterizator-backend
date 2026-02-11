package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to generate shift instances for a location and month")
public record GenerateMonthRequestDto(
        @Schema(description = "Location ID", example = "1", required = true)
        @NotNull(message = "Location ID is required")
        Long locationId,
        @Schema(description = "Year (2020-2100)", example = "2025", required = true)
        @NotNull(message = "Year is required")
        @Min(2020)
        @Max(2100)
        Integer year,
        @Schema(description = "Month (1-12)", example = "2", required = true)
        @NotNull(message = "Month is required")
        @Min(1)
        @Max(12)
        Integer month
) {
}
