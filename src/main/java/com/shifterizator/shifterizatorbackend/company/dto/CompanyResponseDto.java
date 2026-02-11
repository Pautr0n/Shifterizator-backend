package com.shifterizator.shifterizatorbackend.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Company information response")
public record CompanyResponseDto(
        @Schema(description = "Unique company identifier", example = "1")
        Long id,

        @Schema(description = "Company name", example = "Acme Corp")
        String name,

        @Schema(description = "Legal company name", example = "Acme Corporation S.A.")
        String legalName,

        @Schema(description = "Tax identification number", example = "B123456789")
        String taxId,

        @Schema(description = "Company contact email", example = "contact@acmecorp.com")
        String email,

        @Schema(description = "Company contact phone", example = "+34612345678")
        String phone,

        @Schema(description = "Country", example = "Spain")
        String country,

        @Schema(description = "Whether the company is currently active", example = "true")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt,

        @Schema(description = "Username who created the company", nullable = true)
        String createdBy,

        @Schema(description = "Username who last updated the company", nullable = true)
        String updatedBy
) {
}
