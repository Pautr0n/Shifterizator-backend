package com.shifterizator.shifterizatorbackend.company.validator;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DayOfWeekValidator.class)
@Documented
public @interface ValidDayOfWeek {
    String message() default "Invalid day of week; use MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}