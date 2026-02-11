package com.shifterizator.shifterizatorbackend.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "Employee information response")
public record EmployeeResponseDto(
        @Schema(description = "Unique employee identifier", example = "1")
        Long id,

        @Schema(description = "Employee first name", example = "John")
        String name,

        @Schema(description = "Employee last name", example = "Doe")
        String surname,

        @Schema(description = "Employee email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "Employee phone number", example = "+34612345678")
        String phone,

        @Schema(description = "Position/job title name", example = "Cashier")
        String position,

        @Schema(description = "Set of company names the employee belongs to", example = "[\"Acme Corp\", \"Beta Ltd\"]")
        Set<String> companies,

        @Schema(description = "Set of location names where the employee works", example = "[\"Store A\", \"Store B\"]")
        Set<String> locations,

        @Schema(description = "Set of language names the employee speaks", example = "[\"English\", \"Spanish\"]")
        Set<String> languages,

        @Schema(
                description = "Preferred weekday off (e.g. WEDNESDAY, FRIDAY), or null if not set",
                example = "FRIDAY",
                nullable = true
        )
        /** Preferred weekday off (e.g. WEDNESDAY, FRIDAY), or null if not set. */
        String preferredDayOff,

        @Schema(
                description = "Ordered list of preferred shift template IDs (first = highest preference)",
                example = "[5, 3, 7]"
        )
        /** Ordered list of preferred shift template IDs (first = highest preference). */
        List<Long> preferredShiftTemplateIds,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt

) {
}
