package com.shifterizator.shifterizatorbackend.availability.model;

import java.util.EnumSet;
import java.util.Set;

public enum AvailabilityType {
    AVAILABLE,
    VACATION,
    SICK_LEAVE,
    PERSONAL_LEAVE,
    UNJUSTIFIED_ABSENCE,
    UNAVAILABLE;

    /**
     * Availability types that should block scheduling and manual assignment.
     * Centralised here so all modules share the same behaviour.
     */
    public static Set<AvailabilityType> blockingTypes() {
        return EnumSet.of(
                VACATION,
                SICK_LEAVE,
                PERSONAL_LEAVE,
                UNJUSTIFIED_ABSENCE,
                UNAVAILABLE
        );
    }

    /**
     * Whether this availability should block scheduling / assignment.
     */
    public boolean isBlocking() {
        return blockingTypes().contains(this);
    }
}
