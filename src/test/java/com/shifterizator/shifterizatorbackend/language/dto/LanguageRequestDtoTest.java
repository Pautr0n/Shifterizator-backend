package com.shifterizator.shifterizatorbackend.language.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validDto_shouldHaveNoViolations() {
        LanguageRequestDto dto = new LanguageRequestDto("EN", "English");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankCode_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("", "English");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "code".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullCode_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto(null, "English");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "code".equals(v.getPropertyPath().toString()));
    }

    @Test
    void codeTooShort_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("E", "English");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "code".equals(v.getPropertyPath().toString()));
    }

    @Test
    void codeTooLong_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("ENGLISH1234", "English");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "code".equals(v.getPropertyPath().toString()));
    }

    @Test
    void blankName_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("EN", "");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "name".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nullName_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("EN", null);

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "name".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nameTooShort_shouldFailValidation() {
        LanguageRequestDto dto = new LanguageRequestDto("EN", "E");

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "name".equals(v.getPropertyPath().toString()));
    }

    @Test
    void nameTooLong_shouldFailValidation() {
        String longName = "A".repeat(51);
        LanguageRequestDto dto = new LanguageRequestDto("EN", longName);

        Set<ConstraintViolation<LanguageRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> "name".equals(v.getPropertyPath().toString()));
    }
}
