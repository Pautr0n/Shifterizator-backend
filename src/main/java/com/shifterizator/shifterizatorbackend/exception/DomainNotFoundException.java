package com.shifterizator.shifterizatorbackend.exception;

/**
 * Base type for domain-specific "not found" errors that should result in HTTP 404.
 */
public abstract class DomainNotFoundException extends RuntimeException {

    protected DomainNotFoundException(String message) {
        super(message);
    }
}

