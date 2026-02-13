package com.shifterizator.shifterizatorbackend.user.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainValidationException;

/**
 * Thrown when attempting to assign a user to an employee whose company does not match
 * any of the employee's companies.
 */
public class UserNotInEmployeeCompanyException extends DomainValidationException {

    public UserNotInEmployeeCompanyException(String message) {
        super(message);
    }
}
