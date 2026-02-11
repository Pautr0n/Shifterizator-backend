package com.shifterizator.shifterizatorbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change the current user's password")
public record ChangePasswordRequestDto(
        @Schema(
                description = "Current password of the user",
                example = "OldPassword123!",
                required = true,
                format = "password"
        )
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(
                description = "New password. Must be 8-20 characters and contain lowercase, uppercase, number, and special character",
                example = "NewPassword123!",
                required = true,
                format = "password",
                minLength = 8,
                maxLength = 20
        )
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
                message = "Password must contain lowercase, uppercase, number and special character"
        )
        String newPassword
) {
}
