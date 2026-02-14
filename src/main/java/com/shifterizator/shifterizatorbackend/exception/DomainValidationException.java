package com.shifterizator.shifterizatorbackend.exception;

public abstract class DomainValidationException extends RuntimeException {

    protected DomainValidationException(String message) {
        super(message);
    }
}

