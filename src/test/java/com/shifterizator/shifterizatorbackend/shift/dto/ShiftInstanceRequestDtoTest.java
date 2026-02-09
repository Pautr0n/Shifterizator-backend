package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftInstanceRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static LocalDate anyDate() {
        return LocalDate.of(2024, 12, 24);
    }

    private static LocalTime anyStartTime() {
        return LocalTime.of(9, 0);
    }

    private static LocalTime anyEndTime() {
        return LocalTime.of(17, 0);
    }

    @Test
    void validDto_shouldHaveNoViolations() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                anyDate(),
                anyStartTime(),
                anyEndTime(),
                3,
                null,
                "Notes"
        );

        Set<ConstraintViolation<ShiftInstanceRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullShiftTemplateId_shouldFailValidation() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                null,
                1L,
                anyDate(),
                anyStartTime(),
                anyEndTime(),
                3,
                null,
                null
        );

        Set<ConstraintViolation<ShiftInstanceRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "shiftTemplateId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullDate_shouldFailValidation() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                null,
                anyStartTime(),
                anyEndTime(),
                3,
                null,
                null
        );

        Set<ConstraintViolation<ShiftInstanceRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "date".equals(v.getPropertyPath().toString()));
    }

    @Test
    void zeroRequiredEmployees_shouldFailValidation() {
        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                anyDate(),
                anyStartTime(),
                anyEndTime(),
                0,
                null,
                null
        );

        Set<ConstraintViolation<ShiftInstanceRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredEmployees".equals(v.getPropertyPath().toString()));
    }
}
