package com.shifterizator.shifterizatorbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User data in API responses")
public record UserResponseDto(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Username") String username,
        @Schema(description = "Email") String email,
        @Schema(description = "Phone") String phone,
        @Schema(description = "Role") String role,
        @Schema(description = "Company ID") Long companyId,
        @Schema(description = "Whether the user is active") Boolean isActive,
        @Schema(description = "Created by username") String createdBy,
        @Schema(description = "Updated by username") String updatedBy
) {
}
