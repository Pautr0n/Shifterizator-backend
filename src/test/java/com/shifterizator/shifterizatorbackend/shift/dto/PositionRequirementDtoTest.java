package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PositionRequirementDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validDto_shouldHaveNoViolations() {
        PositionRequirementDto dto = new PositionRequirementDto(1L, 2, null);

        Set<ConstraintViolation<PositionRequirementDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullPositionId_shouldFailValidation() {
        PositionRequirementDto dto = new PositionRequirementDto(null, 2, null);

        Set<ConstraintViolation<PositionRequirementDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "positionId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullRequiredCount_shouldFailValidation() {
        PositionRequirementDto dto = new PositionRequirementDto(1L, null, null);

        Set<ConstraintViolation<PositionRequirementDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredCount".equals(v.getPropertyPath().toString()));
    }

    @Test
    void zeroRequiredCount_shouldFailValidation() {
        PositionRequirementDto dto = new PositionRequirementDto(1L, 0, null);

        Set<ConstraintViolation<PositionRequirementDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredCount".equals(v.getPropertyPath().toString()));
    }

    @Test
    void negativeRequiredCount_shouldFailValidation() {
        PositionRequirementDto dto = new PositionRequirementDto(1L, -1, null);

        Set<ConstraintViolation<PositionRequirementDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "requiredCount".equals(v.getPropertyPath().toString()));
    }
}
