package com.shifterizator.shifterizatorbackend.openinghours.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class SpecialOpeningHoursValidationException extends DomainValidationException {

    public SpecialOpeningHoursValidationException(String message) {
        super(message);
    }
}
