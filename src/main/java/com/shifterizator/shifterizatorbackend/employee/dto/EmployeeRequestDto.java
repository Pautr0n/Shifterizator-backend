package com.shifterizator.shifterizatorbackend.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

@Schema(description = "Employee creation or update request")
public record EmployeeRequestDto(
        @Schema(description = "Employee first name", example = "John", required = true)
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Employee last name", example = "Doe", required = true)
        @NotBlank(message = "Surname is required")
        String surname,

        @Schema(description = "Employee email address", example = "john.doe@example.com", format = "email")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Employee phone number", example = "+34612345678")
        String phone,

        @Schema(description = "ID of the position/job title", example = "1", required = true)
        @NotNull(message = "Position ID is required")
        Long positionId,

        @Schema(
                description = "Set of company IDs the employee belongs to. At least one required.",
                example = "[1, 2]",
                required = true
        )
        @NotEmpty(message = "At least one company must be assigned")
        Set<Long> companyIds,

        @Schema(
                description = "Set of location IDs where the employee works",
                example = "[1, 3]"
        )
        Set<Long> locationIds,

        @Schema(
                description = "Set of language IDs the employee speaks",
                example = "[1, 2]"
        )
        Set<Long> languageIds,

        @Schema(
                description = "Preferred weekday off (e.g. WEDNESDAY, FRIDAY). Must be a valid DayOfWeek name.",
                example = "FRIDAY",
                allowableValues = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"}
        )
        /** Optional. Preferred weekday off (e.g. WEDNESDAY, FRIDAY). Must be a valid DayOfWeek name. */
        String preferredDayOff,

        @Schema(
                description = "Ordered list of preferred shift template IDs (first = highest preference)",
                example = "[5, 3, 7]"
        )
        /** Optional. Ordered list of shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds

) {
}
