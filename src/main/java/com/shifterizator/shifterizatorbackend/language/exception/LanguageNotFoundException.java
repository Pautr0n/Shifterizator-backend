package com.shifterizator.shifterizatorbackend.language.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class LanguageNotFoundException extends DomainNotFoundException {

    public LanguageNotFoundException(String message) {
        super(message);
    }
}
