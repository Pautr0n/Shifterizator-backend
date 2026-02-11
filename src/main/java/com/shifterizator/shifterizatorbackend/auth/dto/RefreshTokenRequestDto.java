package com.shifterizator.shifterizatorbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Refresh token request to obtain a new access token")
public record RefreshTokenRequestDto(
        @Schema(
                description = "Valid refresh token obtained from login",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVpZCI6MiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3NzA3Mjc0NDEsImV4cCI6MTc3MTMzMjI0MX0.oRDSDgc5lpJda6UOYP5L9McY49ibN-DcVuTchT5rQR8",
                required = true
        )
        String refreshToken

) {
}
