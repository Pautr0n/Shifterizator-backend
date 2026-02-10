package com.shifterizator.shifterizatorbackend.availability.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class AvailabilityValidationException extends DomainValidationException {

    public AvailabilityValidationException(String message) {
        super(message);
    }
}
