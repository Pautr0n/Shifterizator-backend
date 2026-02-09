package com.shifterizator.shifterizatorbackend.shift.dto;

import java.time.LocalDateTime;

public record ShiftAssignmentResponseDto(
        Long id,
        Long shiftInstanceId,
        Long employeeId,
        String employeeName,
        Boolean isConfirmed,
        LocalDateTime assignedAt,
        String assignedBy,
        LocalDateTime confirmedAt
) {
}
