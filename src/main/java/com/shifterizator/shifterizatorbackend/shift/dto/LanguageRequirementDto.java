package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LanguageRequirementDto(
        @NotNull(message = "Language ID is required")
        Long languageId,

        @NotNull(message = "Required count is required")
        @Min(value = 0, message = "Required count must be 0 or positive")
        Integer requiredCount
) {
}
