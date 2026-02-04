package com.shifterizator.shifterizatorbackend.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class ChangePasswordRequestDtoTest {
    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_pass_validation_when_valid() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "NewPass1!"
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_when_currentPassword_blank() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "",
                "NewPass1!"
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_newPassword_blank() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                ""
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_newPassword_no_uppercase_letter() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "p4ssword!"
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
    @Test
    void should_fail_when_newPassword_no_lowercase_letter() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "P4SSWORD!" // no mayúscula, no número, no especial
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
    @Test
    void should_fail_when_newPassword_no_number_character() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "Password!" // no mayúscula, no número, no especial
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_newPassword_no_special_character() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "P4ssword" // no mayúscula, no número, no especial
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_newPassword_invalid_pattern() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "password" // no mayúscula, no número, no especial
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_newPassword_too_short() {
        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "Aa1!" // demasiado corta
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }


}