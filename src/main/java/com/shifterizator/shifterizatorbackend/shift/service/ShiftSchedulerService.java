package com.shifterizator.shifterizatorbackend.shift.service;

import java.time.LocalDate;

public interface ShiftSchedulerService {

    void scheduleDay(Long locationId, LocalDate date);

    void scheduleRange(Long locationId, LocalDate startDate, LocalDate endDate);
}
