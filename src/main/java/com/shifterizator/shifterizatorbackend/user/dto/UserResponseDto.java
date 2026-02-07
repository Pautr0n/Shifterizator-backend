package com.shifterizator.shifterizatorbackend.user.dto;

public record UserResponseDto(Long id,
                              String username,
                              String email,
                              String phone,
                              String role,
                              Long companyId,
                              Boolean isActive,
                              String createdBy,
                              String updatedBy) {
}
