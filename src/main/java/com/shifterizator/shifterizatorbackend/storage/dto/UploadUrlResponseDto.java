package com.shifterizator.shifterizatorbackend.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned upload URL and final public URL for the uploaded file")
public record UploadUrlResponseDto(
        @Schema(description = "Presigned PUT URL; use with method PUT and body = file, header Content-Type = contentType")
        String uploadUrl,

        @Schema(description = "Final public URL to store in the database (user or employee profilePictureUrl)")
        String finalUrl
) {
}
