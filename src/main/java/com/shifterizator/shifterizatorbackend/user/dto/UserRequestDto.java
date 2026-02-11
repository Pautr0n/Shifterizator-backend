package com.shifterizator.shifterizatorbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a user")
public record UserRequestDto(

        @Schema(description = "Username (4-15 chars)", example = "jdoe", required = true)
        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 15, message = "Username must be between 4 and 15 characters")
        String username,

        @Schema(description = "Email address", example = "jdoe@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Password (8-20 chars; must include lower, upper, number, special)", required = true)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
                message = "Password must contain lowercase, uppercase, number and special character"
        )
        String password,

        @Schema(description = "Role (e.g. SUPERADMIN, COMPANYADMIN, LOCATIONADMIN, EMPLOYEE)", example = "EMPLOYEE", required = true)
        @NotBlank(message = "Role is required")
        String role,

        @Schema(description = "Company ID (required for company-scoped roles)")
        Long companyId,

        @Schema(description = "Phone number")
        String phone

) {
}
