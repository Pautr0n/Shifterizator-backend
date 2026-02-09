package com.shifterizator.shifterizatorbackend.shift.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftAssignmentRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validDto_shouldHaveNoViolations() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);

        Set<ConstraintViolation<ShiftAssignmentRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullShiftInstanceId_shouldFailValidation() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(null, 1L);

        Set<ConstraintViolation<ShiftAssignmentRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "shiftInstanceId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullEmployeeId_shouldFailValidation() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, null);

        Set<ConstraintViolation<ShiftAssignmentRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "employeeId".equals(v.getPropertyPath().toString()));
    }
}
