package com.shifterizator.shifterizatorbackend.availability.controller;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.mapper.AvailabilityMapper;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> create(@Valid @RequestBody AvailabilityRequestDto dto) {
        EmployeeAvailability availability = availabilityService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityMapper.toDto(availability));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> update(@PathVariable Long id, @Valid @RequestBody AvailabilityRequestDto dto) {
        EmployeeAvailability availability = availabilityService.update(id, dto);
        return ResponseEntity.ok(availabilityMapper.toDto(availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @RequestParam(defaultValue = "false") boolean hardDelete) {
        availabilityService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> findById(@PathVariable Long id) {
        EmployeeAvailability availability = availabilityService.findById(id);
        return ResponseEntity.ok(availabilityMapper.toDto(availability));
    }

    @GetMapping
    public ResponseEntity<Page<AvailabilityResponseDto>> search(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) AvailabilityType type,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                availabilityService.search(employeeId, type, locationId, start, end, pageable)
                        .map(availabilityMapper::toDto)
        );
    }

    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<AvailabilityResponseDto>> findByEmployee(@PathVariable Long employeeId) {
        List<EmployeeAvailability> list = availabilityService.findByEmployee(employeeId);
        return ResponseEntity.ok(list.stream().map(availabilityMapper::toDto).toList());
    }

    @GetMapping("/range")
    public ResponseEntity<List<AvailabilityResponseDto>> findByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<EmployeeAvailability> list = availabilityService.findByRange(start, end);
        return ResponseEntity.ok(list.stream().map(availabilityMapper::toDto).toList());
    }
}
