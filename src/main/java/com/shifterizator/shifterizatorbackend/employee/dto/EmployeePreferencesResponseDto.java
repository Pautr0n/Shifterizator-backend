package com.shifterizator.shifterizatorbackend.employee.dto;

import java.util.List;

/**
 * Response DTO for an employee's preferences.
 */
public record EmployeePreferencesResponseDto(
        /** Preferred weekday off (e.g. WEDNESDAY, FRIDAY), or null if not set. */
        String preferredDayOff,
        /** Ordered list of preferred shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds,
        Integer shiftsPerWeek
) {
}
