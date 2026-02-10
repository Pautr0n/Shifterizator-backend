package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class ShiftAssignmentNotFoundException extends DomainNotFoundException {

    public ShiftAssignmentNotFoundException(String message) {
        super(message);
    }
}
