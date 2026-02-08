package com.shifterizator.shifterizatorbackend.availability.dto;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailabilityRequestDto(
        @NotNull(message = "Employee ID is required")
        Long employeeId,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotNull(message = "Availability type is required")
        AvailabilityType type
) {
}
