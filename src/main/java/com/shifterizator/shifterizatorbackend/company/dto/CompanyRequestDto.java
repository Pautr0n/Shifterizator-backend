package com.shifterizator.shifterizatorbackend.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequestDto(@NotBlank(message = "Company's name cannot be empty or just blank spaces")
                                @Size(min = 4, max = 20, message = "Company's name must have between 4 and 20 characters")
                                String name,

                                @Size(min = 4, max = 50, message = "Company's legal name must have between 4 and 50 characters")
                                String legalName,

                                @NotBlank
                                @Size(min = 9, max = 12, message = "Legal tax id must have between 9 and 12 characters")
                                String taxId,

                                @Email(message = "Invalid e-mail format")
                                @NotBlank
                                String email,

                                @Size(min = 9, max = 15, message = "Phone number must have between 9 and 15 characters")
                                String phone
) {
}
