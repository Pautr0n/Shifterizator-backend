package com.shifterizator.shifterizatorbackend.shift.exception;

import com.shifterizator.shifterizatorbackend.exception.ConflictException;

public class ScheduleDaySkippedException extends ConflictException {
    public ScheduleDaySkippedException(String message) {
        super(message);
    }
}
