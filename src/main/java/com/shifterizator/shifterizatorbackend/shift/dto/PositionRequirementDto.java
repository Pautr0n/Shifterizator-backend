package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PositionRequirementDto(
        @NotNull(message = "Position ID is required")
        Long positionId,

        @NotNull(message = "Required count is required")
        @Positive(message = "Required count must be positive")
        Integer requiredCount,

        Integer idealCount
) {
}
