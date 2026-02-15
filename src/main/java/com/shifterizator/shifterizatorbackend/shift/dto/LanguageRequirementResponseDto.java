package com.shifterizator.shifterizatorbackend.shift.dto;

public record LanguageRequirementResponseDto(
        Long languageId,
        String languageName,
        Integer requiredCount
) {
}
