package com.shifterizator.shifterizatorbackend.openinghours.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class SpecialOpeningHoursNotFoundException extends DomainNotFoundException {

    public SpecialOpeningHoursNotFoundException(String message) {
        super(message);
    }
}
