package com.shifterizator.shifterizatorbackend.openinghours.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialOpeningHoursRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static LocalDate anyDate() {
        return LocalDate.of(2024, 12, 24);
    }

    @Test
    void validDto_shouldHaveNoViolations() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                anyDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Christmas Eve",
                "#FF0000",
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullLocationId_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                null,
                anyDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Reason",
                null,
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "locationId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullDate_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                null,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Reason",
                null,
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "date".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullOpenTime_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                anyDate(),
                null,
                LocalTime.of(18, 0),
                "Reason",
                null,
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "openTime".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullCloseTime_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                anyDate(),
                LocalTime.of(9, 0),
                null,
                "Reason",
                null,
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "closeTime".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullReason_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                anyDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                null,
                null,
                false
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "reason".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullAppliesToCompany_shouldFailValidation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                anyDate(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Reason",
                null,
                null
        );

        Set<ConstraintViolation<SpecialOpeningHoursRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "appliesToCompany".equals(v.getPropertyPath().toString()));
    }
}

