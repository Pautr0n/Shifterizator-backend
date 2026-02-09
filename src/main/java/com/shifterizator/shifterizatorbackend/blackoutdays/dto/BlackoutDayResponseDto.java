package com.shifterizator.shifterizatorbackend.blackoutdays.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BlackoutDayResponseDto(
        Long id,
        Long locationId,
        String locationName,
        LocalDate date,
        String reason,
        Boolean appliesToCompany,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
