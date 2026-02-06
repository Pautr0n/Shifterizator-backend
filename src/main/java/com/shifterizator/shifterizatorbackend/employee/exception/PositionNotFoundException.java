package com.shifterizator.shifterizatorbackend.employee.exception;

public class PositionNotFoundException extends RuntimeException {
    public PositionNotFoundException(String message) {
        super(message);
    }
}
