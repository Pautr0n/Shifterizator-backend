package com.shifterizator.shifterizatorbackend.employee.dto;

import java.util.List;

public record EmployeePreferencesResponseDto(
        String preferredDayOff,
        List<Long> preferredShiftTemplateIds,
        Integer shiftsPerWeek
) {
}
