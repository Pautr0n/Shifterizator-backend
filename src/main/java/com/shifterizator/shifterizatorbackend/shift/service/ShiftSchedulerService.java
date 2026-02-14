package com.shifterizator.shifterizatorbackend.shift.service;

import java.time.LocalDate;
import java.time.YearMonth;

public interface ShiftSchedulerService {

    void scheduleDay(Long locationId, LocalDate date);

    void scheduleMonth(Long locationId, YearMonth yearMonth);
}
