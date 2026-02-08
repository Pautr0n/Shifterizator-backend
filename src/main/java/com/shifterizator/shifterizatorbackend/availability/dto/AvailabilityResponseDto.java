package com.shifterizator.shifterizatorbackend.availability.dto;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AvailabilityResponseDto(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        AvailabilityType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
