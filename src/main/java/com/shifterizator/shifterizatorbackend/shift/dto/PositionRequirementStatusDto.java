package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Per-position requirement fulfillment for a shift instance")
public record PositionRequirementStatusDto(
        @Schema(description = "Position ID") Long positionId,
        @Schema(description = "Position name") String positionName,
        @Schema(description = "Minimum required count") Integer requiredCount,
        @Schema(description = "Ideal count") Integer idealCount,
        @Schema(description = "Currently assigned count") Integer assignedCount
) {
}
