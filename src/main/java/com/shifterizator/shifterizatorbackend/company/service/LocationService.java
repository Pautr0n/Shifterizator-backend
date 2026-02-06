package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.model.Location;

import java.util.List;

public interface LocationService {

    Location create(LocationRequestDto dto);

    Location update(Long id, LocationRequestDto dto);

    void delete(Long id);

    Location findById(Long id);

    List<Location> findByCompany(Long companyId);

}
