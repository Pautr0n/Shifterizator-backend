package com.shifterizator.shifterizatorbackend.employee.dto;

import java.util.List;

/**
 * DTO for updating only an employee's preferences (preferred day off and preferred shift templates).
 */
public record EmployeePreferencesRequestDto(
        /** Optional. Preferred weekday off (e.g. WEDNESDAY, FRIDAY). Must be a valid DayOfWeek name. */
        String preferredDayOff,
        /** Optional. Ordered list of shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds
) {
}
