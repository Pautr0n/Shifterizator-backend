package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;

import java.util.List;

public interface LocationService {

    LocationResponseDto create(LocationRequestDto dto);

    LocationResponseDto update(Long id, LocationRequestDto dto);

    void delete(Long id);

    LocationResponseDto findById(Long id);

    List<LocationResponseDto> findByCompany(Long companyId);

}
