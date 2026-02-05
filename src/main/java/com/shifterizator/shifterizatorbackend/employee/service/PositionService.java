package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;

import java.util.List;

public interface PositionService {

    PositionDto create(String name, Long companyId);

    PositionDto update(Long id, String name);

    void delete(Long id);

    PositionDto findById(Long id);

    List<PositionDto> findByCompany(Long companyId);

}
