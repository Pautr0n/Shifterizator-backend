package com.shifterizator.shifterizatorbackend.shift.service;

import java.time.LocalDate;

public interface ScheduleDayRunner {

    void runScheduleDay(Long locationId, LocalDate date);
}
