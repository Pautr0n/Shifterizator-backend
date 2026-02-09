package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;

import java.time.YearMonth;
import java.util.List;

/**
 * Generates shift instances for a location and month using templates,
 * respecting BlackoutDays (no shifts) and SpecialOpeningHours (one shift per day with open/close times).
 */
public interface ShiftGenerationService {

    /**
     * Generates shift instances for the given location and month.
     * For each day: existing instances for that (location, date) are soft-deleted first.
     * - BlackoutDay: no new instances.
     * - SpecialOpeningHours: one instance with openTime/closeTime (using first template).
     * - Otherwise: one instance per active template with template times.
     *
     * @param locationId the location to generate for
     * @param yearMonth  the month to generate
     * @return list of newly created shift instances (not soft-deleted)
     */
    List<ShiftInstance> generateMonth(Long locationId, YearMonth yearMonth);
}
