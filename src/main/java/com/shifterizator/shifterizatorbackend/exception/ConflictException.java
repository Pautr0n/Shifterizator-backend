package com.shifterizator.shifterizatorbackend.exception;

/**
 * Base type for "already exists" or conflict errors that should result in HTTP 409.
 */
public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }
}

