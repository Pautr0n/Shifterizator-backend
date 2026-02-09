package com.shifterizator.shifterizatorbackend.shift.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ShiftAssignmentResponseDto(
        Long id,
        Long shiftInstanceId,
        Long employeeId,
        String employeeName,
        Boolean isConfirmed,
        LocalDateTime assignedAt,
        String assignedBy,
        LocalDateTime confirmedAt,
        /** Preference-related warnings (e.g. assigned on preferred day off). Never null. */
        List<String> warnings
) {
}
