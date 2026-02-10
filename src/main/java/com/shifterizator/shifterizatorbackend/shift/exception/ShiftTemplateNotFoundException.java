package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class ShiftTemplateNotFoundException extends DomainNotFoundException {

    public ShiftTemplateNotFoundException(String message) {
        super(message);
    }
}
