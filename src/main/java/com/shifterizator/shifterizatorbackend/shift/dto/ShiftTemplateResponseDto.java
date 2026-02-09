package com.shifterizator.shifterizatorbackend.shift.dto;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ShiftTemplateResponseDto(
        Long id,
        Long locationId,
        String locationName,
        List<PositionRequirementResponseDto> requiredPositions,
        LocalTime startTime,
        LocalTime endTime,
        Integer totalRequiredEmployees,
        Integer idealEmployees,
        String description,
        Set<String> requiredLanguages,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
