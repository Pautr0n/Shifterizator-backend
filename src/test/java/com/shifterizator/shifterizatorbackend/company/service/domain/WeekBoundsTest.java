package com.shifterizator.shifterizatorbackend.company.service.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeekBoundsTest {

    @Test
    void weekStart_shouldReturnMondayWhenFirstDayIsMonday() {
        LocalDate wed = LocalDate.of(2025, 1, 8);
        LocalDate start = WeekBounds.weekStart(wed, DayOfWeek.MONDAY);
        assertThat(start).isEqualTo(LocalDate.of(2025, 1, 6));
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void weekStart_shouldUseMondayWhenFirstDayIsNull() {
        LocalDate wed = LocalDate.of(2025, 1, 8);
        LocalDate start = WeekBounds.weekStart(wed, null);
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    void weekStart_shouldReturnSundayWhenFirstDayIsSunday() {
        LocalDate wed = LocalDate.of(2025, 1, 8);
        LocalDate start = WeekBounds.weekStart(wed, DayOfWeek.SUNDAY);
        assertThat(start).isEqualTo(LocalDate.of(2025, 1, 5));
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    void weekEnd_shouldBeSixDaysAfterStart() {
        LocalDate date = LocalDate.of(2025, 1, 10);
        LocalDate start = WeekBounds.weekStart(date, DayOfWeek.MONDAY);
        LocalDate end = WeekBounds.weekEnd(date, DayOfWeek.MONDAY);
        assertThat(end).isEqualTo(start.plusDays(6));
    }
}
