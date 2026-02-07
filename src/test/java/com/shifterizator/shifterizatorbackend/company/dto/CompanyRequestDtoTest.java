package com.shifterizator.shifterizatorbackend.company.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<CompanyRequestDto>> violations;

    @Test
    void create_requestdto_with_empty_name_should_fail() {
        CompanyRequestDto requestDto = new CompanyRequestDto(""
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("name")));


    }

    @Test
    void create_requestdto_with_a_name_less_than_3_or_more_than_20_chars_should_fail() {
        CompanyRequestDto requestDto1 = new CompanyRequestDto("Als"
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        CompanyRequestDto requestDto2 = new CompanyRequestDto("012345678901234567890"
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto1);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("name")));

        violations = validator.validate(requestDto2);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("name")));

    }

    @Test
    void create_requestdto_with_a_legalname_less_than_3_or_more_than_20_chars_should_fail() {
        CompanyRequestDto requestDto1 = new CompanyRequestDto("Tron"
                , "Als"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        CompanyRequestDto requestDto2 = new CompanyRequestDto("01234567890123456789"
                , "0123456789012345678901234567890123456789012345678901234567890"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto1);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("legalName")));

        violations = validator.validate(requestDto2);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("legalName")));

    }

    @Test
    void create_requestdto_without_taxid_or_tax_id_less_than_9_or_greather_than_12_should_fail(){

        CompanyRequestDto requestDto1 = new CompanyRequestDto("1234"
                , "Tron"
                , "123456789102T"
                , "test@test.com"
                , "+3499958898"
                , null);

        CompanyRequestDto requestDto2 = new CompanyRequestDto("1234"
                , "Tron"
                , "123456T"
                , "test@test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto1);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("taxId")));

        violations = validator.validate(requestDto2);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("taxId")));

    }

    @Test
    void create_requestdto_with_blank_or_invalid_emial_format_should_fail(){

        CompanyRequestDto requestDto1 = new CompanyRequestDto("1234"
                , "Tron"
                , "123456789T"
                , ""
                , "+3499958898"
                , null);

        CompanyRequestDto requestDto2 = new CompanyRequestDto("1234"
                , "Tron"
                , "123456123T"
                , "test-test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto1);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("email")));

        violations = validator.validate(requestDto2);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("email")));

    }

    @Test
    void create_requestdto_with_phone_less_than_9_or_greather_than_15_should_fail(){

        CompanyRequestDto requestDto1 = new CompanyRequestDto("1234"
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+34999"
                , null);

        CompanyRequestDto requestDto2 = new CompanyRequestDto("1234"
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+34999588988888888888888"
                , null);

        violations = validator.validate(requestDto1);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("phone")));

        violations = validator.validate(requestDto2);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("phone")));

    }

    @Test
    void create_requestdto_with_valid_data_should_succeed(){
        CompanyRequestDto requestDto = new CompanyRequestDto("123456"
                , "Tron"
                , "12345678T"
                , "test@test.com"
                , "+3499958898"
                , null);

        violations = validator.validate(requestDto);
        assertTrue(violations.isEmpty());

    }



}