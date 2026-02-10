package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
