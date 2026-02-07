package com.shifterizator.shifterizatorbackend.user.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class UserRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void should_pass_validation_when_valid() {
        UserRequestDto dto = new UserRequestDto(
                "john123",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void should_fail_when_username_blank() {
        UserRequestDto dto = new UserRequestDto(
                "",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_username_too_short() {
        UserRequestDto dto = new UserRequestDto(
                "abc",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_email_invalid() {
        UserRequestDto dto = new UserRequestDto(
                "john123",
                "not-an-email",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_password_invalid_pattern() {
        UserRequestDto dto = new UserRequestDto(
                "john123",
                "john@mail.com",
                "password",
                "EMPLOYEE",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void should_fail_when_role_blank() {
        UserRequestDto dto = new UserRequestDto(
                "john123",
                "john@mail.com",
                "Password1!",
                "",
                1L,
                null
        );

        Set violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }


}