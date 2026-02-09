package com.shifterizator.shifterizatorbackend.openinghours.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record SpecialOpeningHoursRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Open time is required")
        LocalTime openTime,

        @NotNull(message = "Close time is required")
        LocalTime closeTime,

        @NotNull(message = "Reason is required")
        @Size(max = 200, message = "Reason must not exceed 200 characters")
        String reason,

        @Size(max = 20, message = "Color code must not exceed 20 characters")
        String colorCode,

        @NotNull(message = "Applies to company flag is required")
        Boolean appliesToCompany
) {
}
