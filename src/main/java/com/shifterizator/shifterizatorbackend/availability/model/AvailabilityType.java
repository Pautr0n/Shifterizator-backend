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

    public static Set<AvailabilityType> blockingTypes() {
        return EnumSet.of(
                VACATION,
                SICK_LEAVE,
                PERSONAL_LEAVE,
                UNJUSTIFIED_ABSENCE,
                UNAVAILABLE
        );
    }

    public boolean isBlocking() {
        return blockingTypes().contains(this);
    }
}
