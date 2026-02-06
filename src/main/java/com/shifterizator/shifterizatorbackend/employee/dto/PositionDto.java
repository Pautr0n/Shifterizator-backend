package com.shifterizator.shifterizatorbackend.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PositionDto(

        Long id,
        @NotBlank(message = "Position name is required")
        String name,
        @NotNull(message = "Company ID is required")
        Long companyId

) {
}
