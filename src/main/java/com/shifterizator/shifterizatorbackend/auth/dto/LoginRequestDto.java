package com.shifterizator.shifterizatorbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials for user authentication")
public record LoginRequestDto(
        @Schema(
                description = "Username of the user",
                example = "admin",
                required = true
        )
        @NotBlank
        String username,

        @Schema(
                description = "User password",
                example = "Admin123!",
                required = true,
                format = "password"
        )
        @NotBlank(message = "Password is required")
        String password
) {
}
