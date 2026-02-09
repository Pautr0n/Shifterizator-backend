package com.shifterizator.shifterizatorbackend.shift.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record ShiftInstanceResponseDto(
        Long id,
        Long shiftTemplateId,
        Long locationId,
        String locationName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer requiredEmployees,
        Integer idealEmployees,
        Integer assignedEmployees,
        Boolean isComplete,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
