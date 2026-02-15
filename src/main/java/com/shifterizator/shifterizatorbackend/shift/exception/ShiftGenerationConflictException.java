package com.shifterizator.shifterizatorbackend.shift.exception;

import java.util.List;
import java.time.LocalDate;

public class ShiftGenerationConflictException extends RuntimeException {

    private final List<LocalDate> existingDates;

    public ShiftGenerationConflictException(String message, List<LocalDate> existingDates) {
        super(message);
        this.existingDates = existingDates != null ? List.copyOf(existingDates) : List.of();
    }

    public List<LocalDate> getExistingDates() {
        return existingDates;
    }
}
