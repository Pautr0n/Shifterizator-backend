package com.shifterizator.shifterizatorbackend.language.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a supported language")
public record LanguageRequestDto(
        @Schema(description = "Language code (e.g. en, es)", example = "en", required = true)
        @NotBlank(message = "Language code is required")
        @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
        String code,

        @Schema(description = "Display name", example = "English", required = true)
        @NotBlank(message = "Language name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name
) {
}
