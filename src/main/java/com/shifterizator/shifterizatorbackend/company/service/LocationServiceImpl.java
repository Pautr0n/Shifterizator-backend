package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import jakarta.persistence.EntityNotFoundException;
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
    private final LocationMapper locationMapper;

    @Override
    public LocationResponseDto create(LocationRequestDto dto) {

        Company company = companyRepository.findById(dto.companyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Location location = Location.builder()
                .name(dto.name())
                .address(dto.address())
                .company(company)
                .build();

        locationRepository.save(location);

        return locationMapper.toDto(location);
    }

    @Override
    public LocationResponseDto update(Long id, LocationRequestDto dto) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        location.setName(dto.name());
        location.setAddress(dto.address());

        return locationMapper.toDto(location);
    }

    @Override
    public void delete(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        locationRepository.delete(location);
    }

    @Override
    public LocationResponseDto findById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));

        return locationMapper.toDto(location);
    }

    @Override
    public List<LocationResponseDto> findByCompany(Long companyId) {
        return locationRepository.findAll().stream()
                .filter(l -> l.getCompany().getId().equals(companyId))
                .map(locationMapper::toDto)
                .toList();
    }
}
