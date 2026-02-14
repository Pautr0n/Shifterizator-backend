package com.shifterizator.shifterizatorbackend.company.service.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Week boundaries for a given date and configurable first day of week.
 * Used for "max N shifts per week" and any week-based rules.
 */
public final class WeekBounds {

    private WeekBounds() {}

    /** First day of week; if null, use Monday (ISO). */
    private static final DayOfWeek DEFAULT_FIRST_DAY = DayOfWeek.MONDAY;

    /**
     * Start (inclusive) of the week containing {@code date}.
     * @param date any date
     * @param firstDayOfWeek MONDAY, SUNDAY, etc.; null = Monday
     */
    public static LocalDate weekStart(LocalDate date, DayOfWeek firstDayOfWeek) {
        DayOfWeek first = firstDayOfWeek != null ? firstDayOfWeek : DEFAULT_FIRST_DAY;
        int daysSinceStart = (date.getDayOfWeek().getValue() - first.getValue() + 7) % 7;
        return date.minusDays(daysSinceStart);
    }

    /**
     * End (inclusive) of the week containing {@code date}.
     */
    public static LocalDate weekEnd(LocalDate date, DayOfWeek firstDayOfWeek) {
        return weekStart(date, firstDayOfWeek).plusDays(6);
    }

}
