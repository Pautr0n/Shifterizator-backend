package com.shifterizator.shifterizatorbackend.language.dto;

import java.time.LocalDateTime;

public record LanguageResponseDto(
        Long id,
        String code,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
