package com.shifterizator.shifterizatorbackend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Company creation or update request")
public record CompanyRequestDto(
        @Schema(
                description = "Company name (display name)",
                example = "Acme Corp",
                required = true,
                minLength = 4,
                maxLength = 20
        )
        @NotBlank(message = "Company's name cannot be empty or just blank spaces")
        @Size(min = 4, max = 20, message = "Company's name must have between 4 and 20 characters")
        String name,

        @Schema(
                description = "Legal/registered company name",
                example = "Acme Corporation S.A.",
                minLength = 4,
                maxLength = 50
        )
        @Size(min = 4, max = 50, message = "Company's legal name must have between 4 and 50 characters")
        String legalName,

        @Schema(
                description = "Tax identification number (must be unique)",
                example = "B123456789",
                required = true,
                minLength = 9,
                maxLength = 12
        )
        @NotBlank
        @Size(min = 9, max = 12, message = "Legal tax id must have between 9 and 12 characters")
        String taxId,

        @Schema(
                description = "Company contact email (must be unique)",
                example = "contact@acmecorp.com",
                required = true,
                format = "email"
        )
        @Email(message = "Invalid e-mail format")
        @NotBlank
        String email,

        @Schema(
                description = "Company contact phone number",
                example = "+34612345678",
                minLength = 9,
                maxLength = 15
        )
        @Size(min = 9, max = 15, message = "Phone number must have between 9 and 15 characters")
        String phone,

        @Schema(
                description = "Country where the company is located",
                example = "Spain"
        )
        String country
) {
}
