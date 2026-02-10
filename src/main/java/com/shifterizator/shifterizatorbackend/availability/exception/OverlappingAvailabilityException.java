package com.shifterizator.shifterizatorbackend.availability.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class OverlappingAvailabilityException extends DomainValidationException {

    public OverlappingAvailabilityException(String message) {
        super(message);
    }
}
