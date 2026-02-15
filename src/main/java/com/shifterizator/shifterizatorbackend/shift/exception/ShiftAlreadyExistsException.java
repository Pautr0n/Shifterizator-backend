package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class ShiftAlreadyExistsException extends ConflictException {

    public ShiftAlreadyExistsException(String message) {
        super(message);
    }
}
