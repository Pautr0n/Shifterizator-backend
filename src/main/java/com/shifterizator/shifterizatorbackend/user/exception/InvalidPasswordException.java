package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class InvalidPasswordException extends DomainValidationException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
