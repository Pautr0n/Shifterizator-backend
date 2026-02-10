package com.shifterizator.shifterizatorbackend.company.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class CompanyNotFoundException extends DomainNotFoundException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
