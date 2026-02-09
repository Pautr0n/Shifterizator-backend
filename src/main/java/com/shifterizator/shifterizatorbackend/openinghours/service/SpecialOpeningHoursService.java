package com.shifterizator.shifterizatorbackend.openinghours.service;

import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;
import java.util.List;

public interface SpecialOpeningHoursService {

    SpecialOpeningHours create(SpecialOpeningHoursRequestDto dto);

    SpecialOpeningHours update(Long id, SpecialOpeningHoursRequestDto dto);

    void delete(Long id, boolean hardDelete);

    SpecialOpeningHours findById(Long id);

    Page<SpecialOpeningHours> search(Long locationId, Long companyId, Pageable pageable);

    List<SpecialOpeningHours> findByLocation(Long locationId);

    List<SpecialOpeningHours> findByLocationAndMonth(Long locationId, YearMonth yearMonth);
}
