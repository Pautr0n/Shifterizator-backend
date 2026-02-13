package com.shifterizator.shifterizatorbackend.employee.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

/**
 * Thrown when attempting to assign a user to an employee when that user is already
 * linked to another employee.
 */
public class UserAlreadyAssignedToEmployeeException extends ConflictException {

    public UserAlreadyAssignedToEmployeeException(String message) {
        super(message);
    }
}
