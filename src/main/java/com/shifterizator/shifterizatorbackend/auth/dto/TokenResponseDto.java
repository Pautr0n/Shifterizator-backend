package com.shifterizator.shifterizatorbackend.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        Long userId,
        String username,
        String role,
        Long companyId

) {
}
