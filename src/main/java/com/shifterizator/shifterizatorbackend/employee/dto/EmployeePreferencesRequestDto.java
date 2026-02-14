package com.shifterizator.shifterizatorbackend.employee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record EmployeePreferencesRequestDto(
        String preferredDayOff,
        List<Long> preferredShiftTemplateIds,
        @Positive(message = "Shift per week must be positive")
        @Min(1) @Max(7)
        Integer shiftsPerWeek

) {
}
