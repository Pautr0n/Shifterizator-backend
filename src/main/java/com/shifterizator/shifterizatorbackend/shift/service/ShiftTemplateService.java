package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShiftTemplateService {

    ShiftTemplate create(ShiftTemplateRequestDto dto);

    ShiftTemplate update(Long id, ShiftTemplateRequestDto dto);

    void delete(Long id, boolean hardDelete);

    ShiftTemplate findById(Long id);

    Page<ShiftTemplate> search(Long locationId, Long positionId, Pageable pageable);

    List<ShiftTemplate> findByLocation(Long locationId);
}
