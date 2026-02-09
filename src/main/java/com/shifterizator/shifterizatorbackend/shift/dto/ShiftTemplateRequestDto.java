package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.Set;

public record ShiftTemplateRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Position ID is required")
        Long positionId,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @NotNull(message = "Required employees is required")
        @Positive(message = "Required employees must be positive")
        Integer requiredEmployees,

        @Size(max = 200, message = "Description must not exceed 200 characters")
        String description,

        Set<Long> requiredLanguageIds,

        @NotNull(message = "Is active flag is required")
        Boolean isActive
) {
}
