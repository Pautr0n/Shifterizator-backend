package com.shifterizator.shifterizatorbackend.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Job position (e.g. Cashier, Manager) within a company")
public record PositionDto(

        @Schema(description = "Unique position identifier", example = "1")
        Long id,

        @Schema(description = "Position name", example = "Cashier", required = true)
        @NotBlank(message = "Position name is required")
        String name,

        @Schema(description = "Company ID this position belongs to", example = "1", required = true)
        @NotNull(message = "Company ID is required")
        Long companyId

) {
}
