package com.shifterizator.shifterizatorbackend.blackoutdays.controller;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.mapper.BlackoutDayMapper;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.service.BlackoutDayService;
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
@RequestMapping("/api/blackout-days")
@RequiredArgsConstructor
public class BlackoutDayController {

    private final BlackoutDayService blackoutDayService;
    private final BlackoutDayMapper blackoutDayMapper;

    @PostMapping
    public ResponseEntity<BlackoutDayResponseDto> create(@Valid @RequestBody BlackoutDayRequestDto dto) {
        BlackoutDay blackoutDay = blackoutDayService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(blackoutDayMapper.toDto(blackoutDay));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlackoutDayResponseDto> update(@PathVariable Long id, @Valid @RequestBody BlackoutDayRequestDto dto) {
        BlackoutDay blackoutDay = blackoutDayService.update(id, dto);
        return ResponseEntity.ok(blackoutDayMapper.toDto(blackoutDay));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        blackoutDayService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlackoutDayResponseDto> findById(@PathVariable Long id) {
        BlackoutDay blackoutDay = blackoutDayService.findById(id);
        return ResponseEntity.ok(blackoutDayMapper.toDto(blackoutDay));
    }

    @GetMapping
    public ResponseEntity<Page<BlackoutDayResponseDto>> search(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                blackoutDayService.search(locationId, companyId, pageable)
                        .map(blackoutDayMapper::toDto)
        );
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<BlackoutDayResponseDto>> findByLocation(@PathVariable Long locationId) {
        List<BlackoutDay> list = blackoutDayService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(blackoutDayMapper::toDto).toList());
    }

    @GetMapping("/month")
    public ResponseEntity<List<BlackoutDayResponseDto>> findByLocationAndMonth(
            @RequestParam Long locationId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        List<BlackoutDay> list = blackoutDayService.findByLocationAndMonth(locationId, month);
        return ResponseEntity.ok(list.stream().map(blackoutDayMapper::toDto).toList());
    }
}
