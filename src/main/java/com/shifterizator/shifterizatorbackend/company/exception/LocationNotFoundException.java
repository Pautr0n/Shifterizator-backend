package com.shifterizator.shifterizatorbackend.company.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class LocationNotFoundException extends DomainNotFoundException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
