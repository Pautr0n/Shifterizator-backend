package com.shifterizator.shifterizatorbackend.availability.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class AvailabilityNotFoundException extends DomainNotFoundException {

    public AvailabilityNotFoundException(String message) {
        super(message);
    }
}
