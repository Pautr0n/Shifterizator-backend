package com.shifterizator.shifterizatorbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User eligible for assignment (e.g. to an employee), by company")
public record AssignableUserDto(
        @Schema(description = "User ID") Long id,
        @Schema(description = "Username") String username
) {
}
