package com.shifterizator.shifterizatorbackend.company.controller;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public LocationResponseDto create(@Valid @RequestBody LocationRequestDto dto) {
        return locationService.create(dto);
    }

    @PutMapping("/{id}")
    public LocationResponseDto update(@PathVariable Long id, @Valid @RequestBody LocationRequestDto dto) {
        return locationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        locationService.delete(id);
    }

    @GetMapping("/{id}")
    public LocationResponseDto findById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<LocationResponseDto> findByCompany(@PathVariable Long companyId) {
        return locationService.findByCompany(companyId);
    }
}