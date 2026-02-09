package com.shifterizator.shifterizatorbackend.employee.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record EmployeeResponseDto(
        Long id,
        String name,
        String surname,
        String email,
        String phone,
        String position,
        Set<String> companies,
        Set<String> locations,
        Set<String> languages,
        /** Preferred weekday off (e.g. WEDNESDAY, FRIDAY), or null if not set. */
        String preferredDayOff,
        /** Ordered list of preferred shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
