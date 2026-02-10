package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class ShiftValidationException extends DomainValidationException {

    public ShiftValidationException(String message) {
        super(message);
    }
}
