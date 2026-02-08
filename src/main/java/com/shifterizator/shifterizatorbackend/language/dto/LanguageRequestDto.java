package com.shifterizator.shifterizatorbackend.language.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LanguageRequestDto(
        @NotBlank(message = "Language code is required")
        @Size(min = 2, max = 10, message = "Code must be between 2 and 10 characters")
        String code,

        @NotBlank(message = "Language name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name
) {
}
