package com.shifterizator.shifterizatorbackend.shift.dto;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Set;

public record ShiftTemplateResponseDto(
        Long id,
        Long locationId,
        String locationName,
        Long positionId,
        String positionName,
        LocalTime startTime,
        LocalTime endTime,
        Integer requiredEmployees,
        String description,
        Set<String> requiredLanguages,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
