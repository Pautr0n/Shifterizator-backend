package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

public class UserNotInEmployeeCompanyException extends DomainValidationException {

    public UserNotInEmployeeCompanyException(String message) {
        super(message);
    }
}
