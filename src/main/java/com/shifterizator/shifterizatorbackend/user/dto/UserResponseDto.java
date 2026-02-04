package com.shifterizator.shifterizatorbackend.user.dto;

public record UserResponseDto(Long id,
                              String username,
                              String email,
                              String role,
                              Long companyId,
                              Boolean isActive

) {
}
