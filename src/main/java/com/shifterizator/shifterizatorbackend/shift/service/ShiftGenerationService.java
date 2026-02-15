package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface ShiftGenerationService {

    List<ShiftInstance> generateMonth(Long locationId, YearMonth yearMonth);

    List<ShiftInstance> generateRange(Long locationId, LocalDate startDate, LocalDate endDate, boolean replaceExisting);
}
