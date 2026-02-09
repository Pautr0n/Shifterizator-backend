package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftTemplateRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static LocalTime anyStartTime() {
        return LocalTime.of(9, 0);
    }

    private static LocalTime anyEndTime() {
        return LocalTime.of(17, 0);
    }

    @Test
    void validDto_shouldHaveNoViolations() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                10L,
                List.of(
                        new PositionRequirementDto(1L, 2),
                        new PositionRequirementDto(2L, 1)
                ),
                anyStartTime(),
                anyEndTime(),
                "Morning shift",
                Set.of(),
                true
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullLocationId_shouldFailValidation() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                null,
                List.of(new PositionRequirementDto(1L, 1)),
                anyStartTime(),
                anyEndTime(),
                "Test",
                Set.of(),
                true
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "locationId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullRequiredPositions_shouldFailValidation() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                10L,
                null,
                anyStartTime(),
                anyEndTime(),
                "Test",
                Set.of(),
                true
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredPositions".equals(v.getPropertyPath().toString()));
    }

    @Test
    void emptyRequiredPositions_shouldFailValidation() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                10L,
                List.of(),
                anyStartTime(),
                anyEndTime(),
                "Test",
                Set.of(),
                true
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredPositions".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullStartTime_shouldFailValidation() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                10L,
                List.of(new PositionRequirementDto(1L, 1)),
                null,
                anyEndTime(),
                "Test",
                Set.of(),
                true
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "startTime".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullIsActive_shouldFailValidation() {
        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                10L,
                List.of(new PositionRequirementDto(1L, 1)),
                anyStartTime(),
                anyEndTime(),
                "Test",
                Set.of(),
                null
        );

        Set<ConstraintViolation<ShiftTemplateRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "isActive".equals(v.getPropertyPath().toString()));
    }
}
