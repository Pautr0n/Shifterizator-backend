package com.shifterizator.shifterizatorbackend.shift.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ScheduleDayRunnerImpl implements ScheduleDayRunner {

    private final ShiftSchedulerService shiftSchedulerService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runScheduleDay(Long locationId, LocalDate date) {
        shiftSchedulerService.scheduleDay(locationId, date);
    }
}
