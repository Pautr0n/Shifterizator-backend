package com.shifterizator.shifterizatorbackend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update the current user's profile picture URL")
public record ProfilePictureUpdateDto(
        @Schema(description = "Profile picture URL (e.g. from R2 after upload)", example = "https://...")
        @Size(max = 512)
        String profilePictureUrl
) {
}
