package com.shifterizator.shifterizatorbackend.company.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class CompanyValidationException extends DomainValidationException {
    public CompanyValidationException(String message) {
        super(message);
    }
}
