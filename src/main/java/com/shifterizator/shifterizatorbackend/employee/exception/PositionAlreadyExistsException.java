package com.shifterizator.shifterizatorbackend.employee.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class PositionAlreadyExistsException extends ConflictException {
    public PositionAlreadyExistsException(String message) {
        super(message);
    }
}
