package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class UserNotFoundException extends DomainNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
