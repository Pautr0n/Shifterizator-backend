package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Per-language requirement fulfillment for a shift instance")
public record LanguageRequirementStatusDto(
        @Schema(description = "Language ID") Long languageId,
        @Schema(description = "Language name") String languageName,
        @Schema(description = "Required count") Integer requiredCount,
        @Schema(description = "Currently assigned count (employees who speak this language)") Integer assignedCount
) {
}
