package com.shifterizator.shifterizatorbackend.openinghours.controller;

import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
import com.shifterizator.shifterizatorbackend.openinghours.mapper.SpecialOpeningHoursMapper;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.service.SpecialOpeningHoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/opening-hours")
@RequiredArgsConstructor
public class SpecialOpeningHoursController {

    private final SpecialOpeningHoursService openingHoursService;
    private final SpecialOpeningHoursMapper openingHoursMapper;

    @PostMapping
    public ResponseEntity<SpecialOpeningHoursResponseDto> create(@Valid @RequestBody SpecialOpeningHoursRequestDto dto) {
        SpecialOpeningHours openingHours = openingHoursService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(openingHoursMapper.toDto(openingHours));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialOpeningHoursResponseDto> update(@PathVariable Long id, @Valid @RequestBody SpecialOpeningHoursRequestDto dto) {
        SpecialOpeningHours openingHours = openingHoursService.update(id, dto);
        return ResponseEntity.ok(openingHoursMapper.toDto(openingHours));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        openingHoursService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialOpeningHoursResponseDto> findById(@PathVariable Long id) {
        SpecialOpeningHours openingHours = openingHoursService.findById(id);
        return ResponseEntity.ok(openingHoursMapper.toDto(openingHours));
    }

    @GetMapping
    public ResponseEntity<Page<SpecialOpeningHoursResponseDto>> search(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                openingHoursService.search(locationId, companyId, pageable)
                        .map(openingHoursMapper::toDto)
        );
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<SpecialOpeningHoursResponseDto>> findByLocation(@PathVariable Long locationId) {
        List<SpecialOpeningHours> list = openingHoursService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(openingHoursMapper::toDto).toList());
    }

    @GetMapping("/month")
    public ResponseEntity<List<SpecialOpeningHoursResponseDto>> findByLocationAndMonth(
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        List<SpecialOpeningHours> list = openingHoursService.findByLocationAndMonth(locationId, month);
        return ResponseEntity.ok(list.stream().map(openingHoursMapper::toDto).toList());
    }
}
