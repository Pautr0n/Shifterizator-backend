package com.shifterizator.shifterizatorbackend.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EmployeeRequestDto(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Surname is required")
        String surname,

        @Email(message = "Email must be valid")
        String email,

        String phone,

        @NotNull(message = "Position ID is required")
        Long positionId,

        @NotEmpty(message = "At least one company must be assigned")
        Set<Long> companyIds,

        Set<Long> locationIds

) {
}
