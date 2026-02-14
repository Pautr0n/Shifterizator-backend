package com.shifterizator.shifterizatorbackend.employee.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO for updating only an employee's preferences (preferred day off and preferred shift templates).
 */
public record EmployeePreferencesRequestDto(
        /** Optional. Preferred weekday off (e.g. WEDNESDAY, FRIDAY). Must be a valid DayOfWeek name. */
        String preferredDayOff,
        /** Optional. Ordered list of shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds,
        @Positive(message = "Shift per week must be positive")
        @Min(1) @Max(7)
        Integer shiftsPerWeek

) {
}
