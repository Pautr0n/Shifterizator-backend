package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
