package com.shifterizator.shifterizatorbackend.blackoutdays.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class BlackoutDayNotFoundException extends DomainNotFoundException {

    public BlackoutDayNotFoundException(String message) {
        super(message);
    }
}
