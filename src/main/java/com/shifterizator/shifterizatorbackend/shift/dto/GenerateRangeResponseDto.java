package com.shifterizator.shifterizatorbackend.shift.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response after generating shift instances for a date range")
public record GenerateRangeResponseDto(
        @Schema(description = "Number of shift instances created")
        int count,
        @Schema(description = "Created shift instances")
        List<ShiftInstanceResponseDto> instances
) {
}
