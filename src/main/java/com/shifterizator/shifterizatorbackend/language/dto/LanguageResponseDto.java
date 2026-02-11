package com.shifterizator.shifterizatorbackend.language.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Supported language (for localization)")
public record LanguageResponseDto(
        @Schema(description = "Language ID") Long id,
        @Schema(description = "Language code") String code,
        @Schema(description = "Display name") String name,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp") LocalDateTime updatedAt
) {
}
