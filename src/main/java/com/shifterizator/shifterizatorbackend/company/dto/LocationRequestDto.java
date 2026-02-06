package com.shifterizator.shifterizatorbackend.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationRequestDto(
        @NotBlank(message = "Location's name cannot be empty or just blank spaces")
        @Size(min = 4, max = 20, message = "Location's name must have between 4 and 20 characters")
        String name,
        String address,
        @NotNull(message = "Company ID is required")
        Long companyId

) {
}
