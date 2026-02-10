package com.shifterizator.shifterizatorbackend.employee.exception;

import com.shifterizator.shifterizatorbackend.exception.DomainNotFoundException;

public class EmployeeNotFoundException extends DomainNotFoundException {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
