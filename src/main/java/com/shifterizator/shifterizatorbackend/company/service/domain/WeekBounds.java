package com.shifterizator.shifterizatorbackend.company.service.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class WeekBounds {

    private WeekBounds() {}

    private static final DayOfWeek DEFAULT_FIRST_DAY = DayOfWeek.MONDAY;

    public static LocalDate weekStart(LocalDate date, DayOfWeek firstDayOfWeek) {
        DayOfWeek first = firstDayOfWeek != null ? firstDayOfWeek : DEFAULT_FIRST_DAY;
        int daysSinceStart = (date.getDayOfWeek().getValue() - first.getValue() + 7) % 7;
        return date.minusDays(daysSinceStart);
    }

    public static LocalDate weekEnd(LocalDate date, DayOfWeek firstDayOfWeek) {
        return weekStart(date, firstDayOfWeek).plusDays(6);
    }

}
