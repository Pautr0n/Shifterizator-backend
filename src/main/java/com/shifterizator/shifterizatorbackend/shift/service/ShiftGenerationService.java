package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;

import java.time.YearMonth;
import java.util.List;

public interface ShiftGenerationService {

    List<ShiftInstance> generateMonth(Long locationId, YearMonth yearMonth);
}
