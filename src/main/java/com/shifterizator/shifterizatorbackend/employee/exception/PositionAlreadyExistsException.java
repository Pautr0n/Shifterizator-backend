package com.shifterizator.shifterizatorbackend.employee.exception;

public class PositionAlreadyExistsException extends RuntimeException {
    public PositionAlreadyExistsException(String message) {
        super(message);
    }
}
