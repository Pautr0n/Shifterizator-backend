package com.shifterizator.shifterizatorbackend.company.controller;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<LocationResponseDto> create(@Valid @RequestBody LocationRequestDto dto) {
        Location location = locationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationMapper.toDto(location));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> update(@PathVariable Long id, @Valid @RequestBody LocationRequestDto dto) {
        Location location = locationService.update(id, dto);
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> findById(@PathVariable Long id) {
        Location location = locationService.findById(id);
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<LocationResponseDto>> findByCompany(@PathVariable Long companyId) {
        List<Location> locations = locationService.findByCompany(companyId);
        return ResponseEntity.ok(locations.stream().map(locationMapper::toDto).toList());
    }
}