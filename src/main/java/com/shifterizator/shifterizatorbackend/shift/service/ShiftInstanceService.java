package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ShiftInstanceService {

    ShiftInstance create(ShiftInstanceRequestDto dto);

    ShiftInstance update(Long id, ShiftInstanceRequestDto dto);

    void delete(Long id, boolean hardDelete);

    ShiftInstance findById(Long id);

    Page<ShiftInstance> search(Long locationId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<ShiftInstance> findByLocationAndDate(Long locationId, LocalDate date);

    List<ShiftInstance> findByLocationAndDateRange(Long locationId, LocalDate startDate, LocalDate endDate);
}
