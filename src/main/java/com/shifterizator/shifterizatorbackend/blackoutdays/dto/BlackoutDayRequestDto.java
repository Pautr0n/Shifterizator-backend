package com.shifterizator.shifterizatorbackend.blackoutdays.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BlackoutDayRequestDto(
        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Reason is required")
        @Size(max = 200, message = "Reason must not exceed 200 characters")
        String reason,

        @NotNull(message = "Applies to company flag is required")
        Boolean appliesToCompany
) {
}
