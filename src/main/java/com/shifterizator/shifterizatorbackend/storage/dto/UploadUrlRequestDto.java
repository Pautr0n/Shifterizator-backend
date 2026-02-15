package com.shifterizator.shifterizatorbackend.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to obtain a presigned URL for uploading a profile picture")
public record UploadUrlRequestDto(
        @Schema(description = "Scope: user or employee", example = "user", required = true)
        @NotNull(message = "Scope is required")
        @Pattern(regexp = "^(user|employee)$", message = "Scope must be 'user' or 'employee'")
        String scope,

        @Schema(description = "Entity ID: required when scope is 'employee' (employee ID); ignored when scope is 'user'")
        Long entityId,

        @Schema(description = "Image content type", example = "image/jpeg", required = true)
        @NotBlank(message = "Content type is required")
        @Pattern(regexp = "^(image/jpeg|image/png|image/webp)$", message = "Content type must be image/jpeg, image/png, or image/webp")
        String contentType
) {
}
