package com.shifterizator.shifterizatorbackend.exception;

public abstract class DomainNotFoundException extends RuntimeException {

    protected DomainNotFoundException(String message) {
        super(message);
    }
}

