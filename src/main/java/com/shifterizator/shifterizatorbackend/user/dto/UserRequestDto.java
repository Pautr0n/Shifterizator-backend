package com.shifterizator.shifterizatorbackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 15, message = "Username must be between 4 and 15 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
                message = "Password must contain lowercase, uppercase, number and special character"
        )
        String password,

        @NotBlank(message = "Role is required")
        String role,

        Long companyId,

        String phone

) {
}
