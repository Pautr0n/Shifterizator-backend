package com.shifterizator.shifterizatorbackend.employee.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class PositionNotFoundException extends DomainNotFoundException {
    public PositionNotFoundException(String message) {
        super(message);
    }
}
