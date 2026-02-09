package com.shifterizator.shifterizatorbackend.blackoutdays.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutDayRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static LocalDate anyDate() {
        return LocalDate.of(2024, 12, 24);
    }

    @Test
    void validDto_shouldHaveNoViolations() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                anyDate(),
                "Holiday closure",
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullLocationId_shouldFailValidation() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                null,
                anyDate(),
                "Holiday closure",
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "locationId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullDate_shouldFailValidation() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                null,
                "Holiday closure",
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "date".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullReason_shouldFailValidation() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                anyDate(),
                null,
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "reason".equals(v.getPropertyPath().toString()));
    }

    @Test
    void reasonExceedingMaxLength_shouldFailValidation() {
        String longReason = "a".repeat(201);
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                anyDate(),
                longReason,
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "reason".equals(v.getPropertyPath().toString())
                && v.getMessage().contains("200"));
    }

    @Test
    void nullAppliesToCompany_shouldFailValidation() {
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                anyDate(),
                "Holiday closure",
                null
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "appliesToCompany".equals(v.getPropertyPath().toString()));
    }

    @Test
    void reasonAtMaxLength_shouldPassValidation() {
        String maxLengthReason = "a".repeat(200);
        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                10L,
                anyDate(),
                maxLengthReason,
                false
        );

        Set<ConstraintViolation<BlackoutDayRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
