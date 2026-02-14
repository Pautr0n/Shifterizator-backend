package com.shifterizator.shifterizatorbackend.exception;

public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }
}

