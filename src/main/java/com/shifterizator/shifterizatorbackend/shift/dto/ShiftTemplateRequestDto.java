package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record ShiftTemplateRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Required positions are required")
        @NotEmpty(message = "At least one position requirement is required")
        @Valid
        List<PositionRequirementDto> requiredPositions,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Size(max = 200, message = "Description must not exceed 200 characters")
        String description,

        Set<Long> requiredLanguageIds,

        /** Target headcount when enough staff available; must be >= required if set. */
        Integer idealEmployees,

        @NotNull(message = "Is active flag is required")
        Boolean isActive,

        /** Scheduler priority: lower = higher priority. Null = no preference. */
        Integer priority
) {
}
