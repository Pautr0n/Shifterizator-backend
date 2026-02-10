package com.shifterizator.shifterizatorbackend.exception;

/**
 * Base type for domain-specific validation errors that should result in HTTP 400.
 */
public abstract class DomainValidationException extends RuntimeException {

    protected DomainValidationException(String message) {
        super(message);
    }
}

