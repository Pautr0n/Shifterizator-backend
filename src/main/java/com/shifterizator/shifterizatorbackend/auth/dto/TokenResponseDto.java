package com.shifterizator.shifterizatorbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication tokens and user information returned after successful login or token refresh")
public record TokenResponseDto(
        @Schema(
                description = "JWT access token for API authentication. Valid for 15 minutes.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVpZCI6Miwicm9sZSI6IkNPTVBBTllBRE1JTiIsImlhdCI6MTc3MDcyNzQ0MSwiZXhwIjoxNzcwNzI4MzQxfQ.w_fGKECWe-lQCEnZ9zKMFeMWjOMGowrCyBd1ucVsOnc",
                required = true
        )
        String accessToken,

        @Schema(
                description = "JWT refresh token for obtaining new access tokens. Valid for 7 days.",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVpZCI6MiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3NzA3Mjc0NDEsImV4cCI6MTc3MTMzMjI0MX0.oRDSDgc5lpJda6UOYP5L9McY49ibN-DcVuTchT5rQR8",
                required = true
        )
        String refreshToken,

        @Schema(
                description = "Unique identifier of the authenticated user",
                example = "2",
                required = true
        )
        Long userId,

        @Schema(
                description = "Username of the authenticated user",
                example = "admin",
                required = true
        )
        String username,

        @Schema(
                description = "Role of the authenticated user",
                example = "COMPANYADMIN",
                allowableValues = {"SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER", "READONLYMANAGER", "EMPLOYEE"},
                required = true
        )
        String role,

        @Schema(
                description = "Unique identifier of the company the user belongs to. Null for SUPERADMIN users.",
                example = "1",
                nullable = true
        )
        Long companyId

) {
}
