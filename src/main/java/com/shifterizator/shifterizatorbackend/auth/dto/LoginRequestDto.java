package com.shifterizator.shifterizatorbackend.auth.dto;

import jakarta.validation.constraints.NotBlank;


public record LoginRequestDto(
        @NotBlank
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}
