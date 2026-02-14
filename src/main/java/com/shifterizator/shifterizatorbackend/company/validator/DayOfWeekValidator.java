package com.shifterizator.shifterizatorbackend.company.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class DayOfWeekValidator implements ConstraintValidator<ValidDayOfWeek, String> {

    private static final Set<String> VALID = Arrays.stream(java.time.DayOfWeek.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return VALID.contains(value);
    }
}