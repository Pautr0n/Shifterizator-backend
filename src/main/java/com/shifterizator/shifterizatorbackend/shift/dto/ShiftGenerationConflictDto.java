package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.time.LocalDate;

@Schema(description = "Response when generating shifts would replace existing ones; send replaceExisting=true to confirm")
public record ShiftGenerationConflictDto(
        @Schema(description = "Human-readable message")
        String message,
        @Schema(description = "Dates that already have shifts and would be replaced")
        List<LocalDate> existingDates
) {
}
