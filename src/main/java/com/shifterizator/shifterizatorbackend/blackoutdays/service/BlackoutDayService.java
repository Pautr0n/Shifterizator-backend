package com.shifterizator.shifterizatorbackend.blackoutdays.service;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface BlackoutDayService {

    BlackoutDay create(BlackoutDayRequestDto dto);

    BlackoutDay update(Long id, BlackoutDayRequestDto dto);

    void delete(Long id, boolean hardDelete);

    BlackoutDay findById(Long id);

    Page<BlackoutDay> search(Long locationId, Long companyId, Pageable pageable);

    List<BlackoutDay> findByLocation(Long locationId);

    List<BlackoutDay> findByLocationAndMonth(Long locationId, YearMonth yearMonth);

    List<BlackoutDay> findByLocationAndDateRange(Long locationId, LocalDate start, LocalDate end);
}
