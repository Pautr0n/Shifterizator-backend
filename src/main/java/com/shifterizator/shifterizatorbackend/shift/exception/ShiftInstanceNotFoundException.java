package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class ShiftInstanceNotFoundException extends DomainNotFoundException {

    public ShiftInstanceNotFoundException(String message) {
        super(message);
    }
}
