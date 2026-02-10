package com.shifterizator.shifterizatorbackend.blackoutdays.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class BlackoutDayValidationException extends DomainValidationException {

    public BlackoutDayValidationException(String message) {
        super(message);
    }
}
