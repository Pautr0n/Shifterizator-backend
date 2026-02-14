package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final CompanyRepository companyRepository;

    @Override
    public Location create(LocationRequestDto dto) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(dto.companyId())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        Location location = Location.builder()
                .name(dto.name())
                .address(dto.address())
                .company(company)
                .openDaysOfWeek(LocationMapper.toOpenDaysOfWeek(dto.openDaysOfWeek()))
                .firstDayOfWeek(LocationMapper.toFirstDayOfWeek(dto.firstDayOfWeek()))
                .build();

        return locationRepository.save(location);
    }

    @Override
    public Location update(Long id, LocationRequestDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        location.setName(dto.name());
        location.setAddress(dto.address());
        location.setOpenDaysOfWeek(LocationMapper.toOpenDaysOfWeek(dto.openDaysOfWeek()));
        location.setFirstDayOfWeek(LocationMapper.toFirstDayOfWeek(dto.firstDayOfWeek()));

        return location;
    }

    @Override
    public void delete(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        locationRepository.delete(location);
    }

    @Override
    public Location findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        return location;
    }

    @Override
    public List<Location> findByCompany(Long companyId) {
        return locationRepository.findByCompany_Id(companyId);
    }
}
