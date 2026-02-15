package com.shifterizator.shifterizatorbackend.shift.dto;

import com.shifterizator.shifterizatorbackend.shift.dto.LanguageRequirementResponseDto;

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
        List<LanguageRequirementResponseDto> requiredLanguageRequirements,
        Boolean isActive,
        Integer priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
