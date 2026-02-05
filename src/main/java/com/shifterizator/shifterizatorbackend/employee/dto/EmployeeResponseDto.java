package com.shifterizator.shifterizatorbackend.employee.dto;

import java.time.LocalDateTime;
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
