package com.shifterizator.shifterizatorbackend.employee.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class UserAlreadyAssignedToEmployeeException extends ConflictException {

    public UserAlreadyAssignedToEmployeeException(String message) {
        super(message);
    }
}
