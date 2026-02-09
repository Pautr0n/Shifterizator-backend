package com.shifterizator.shifterizatorbackend.openinghours.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SpecialOpeningHoursResponseDto(
        Long id,
        Long locationId,
        String locationName,
        LocalDate date,
        LocalTime openTime,
        LocalTime closeTime,
        String reason,
        String colorCode,
        Boolean appliesToCompany,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
}
