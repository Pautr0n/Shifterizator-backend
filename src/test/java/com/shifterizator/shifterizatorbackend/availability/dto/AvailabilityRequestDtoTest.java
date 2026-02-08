package com.shifterizator.shifterizatorbackend.availability.dto;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static LocalDate future() {
        return LocalDate.now().plusDays(1);
    }

    @Test
    void validDto_shouldHaveNoViolations() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, future(), future().plusDays(5), AvailabilityType.VACATION);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullEmployeeId_shouldFailValidation() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(null, future(), future().plusDays(5), AvailabilityType.VACATION);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "employeeId".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullStartDate_shouldFailValidation() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, null, future().plusDays(5), AvailabilityType.VACATION);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "startDate".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullEndDate_shouldFailValidation() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, future(), null, AvailabilityType.VACATION);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "endDate".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullType_shouldFailValidation() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, future(), future().plusDays(5), null);

        Set<ConstraintViolation<AvailabilityRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "type".equals(v.getPropertyPath().toString()));
    }
}
