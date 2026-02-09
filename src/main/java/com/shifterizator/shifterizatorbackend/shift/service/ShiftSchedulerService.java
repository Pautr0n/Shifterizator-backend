package com.shifterizator.shifterizatorbackend.shift.service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Auto-assigns employees to shift instances following priority rules 0–4:
 * 0. Minimum requirements for every shift must be satisfied.
 * 1. If possible, satisfy ideal for the day (MVP: same as 0).
 * 2. If ideal cannot be met, ensure afternoon shift reaches its target before morning.
 * 3. Ensure at least one manager is present during the day, preferably in the afternoon.
 * 4. Attempt to meet language minimums and distribute languages evenly across shifts.
 */
public interface ShiftSchedulerService {

    /**
     * Runs auto-assignment for all shift instances at the given location on the given date.
     *
     * @param locationId the location
     * @param date       the date
     */
    void scheduleDay(Long locationId, LocalDate date);

    /**
     * Runs auto-assignment for each day in the month at the given location.
     *
     * @param locationId the location
     * @param yearMonth  the month
     */
    void scheduleMonth(Long locationId, YearMonth yearMonth);
}
