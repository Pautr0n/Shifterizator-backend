package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.GenerateMonthRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ScheduleDayRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftGenerationService;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftInstanceService;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftSchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shift-instances")
@RequiredArgsConstructor
public class ShiftInstanceController {

    private final ShiftInstanceService shiftInstanceService;
    private final ShiftInstanceMapper shiftInstanceMapper;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final ShiftGenerationService shiftGenerationService;
    private final ShiftSchedulerService shiftSchedulerService;

    @PostMapping("/generate-month")
    public ResponseEntity<List<ShiftInstanceResponseDto>> generateMonth(@Valid @RequestBody GenerateMonthRequestDto dto) {
        YearMonth yearMonth = YearMonth.of(dto.year(), dto.month());
        List<ShiftInstance> instances = shiftGenerationService.generateMonth(dto.locationId(), yearMonth);
        List<ShiftInstanceResponseDto> body = instances.stream()
                .map(i -> shiftInstanceMapper.toDto(i, shiftInstanceRepository.countActiveAssignments(i.getId())))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/schedule-day")
    public ResponseEntity<Void> scheduleDay(@Valid @RequestBody ScheduleDayRequestDto dto) {
        shiftSchedulerService.scheduleDay(dto.locationId(), dto.date());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/schedule-month")
    public ResponseEntity<Void> scheduleMonth(@Valid @RequestBody GenerateMonthRequestDto dto) {
        shiftSchedulerService.scheduleMonth(dto.locationId(), YearMonth.of(dto.year(), dto.month()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping
    public ResponseEntity<ShiftInstanceResponseDto> create(@Valid @RequestBody ShiftInstanceRequestDto dto) {
        ShiftInstance instance = shiftInstanceService.create(dto);
        int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftInstanceMapper.toDto(instance, assignedCount));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftInstanceResponseDto> update(@PathVariable Long id, @Valid @RequestBody ShiftInstanceRequestDto dto) {
        ShiftInstance instance = shiftInstanceService.update(id, dto);
        int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
        return ResponseEntity.ok(shiftInstanceMapper.toDto(instance, assignedCount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        shiftInstanceService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftInstanceResponseDto> findById(@PathVariable Long id) {
        ShiftInstance instance = shiftInstanceService.findById(id);
        int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
        return ResponseEntity.ok(shiftInstanceMapper.toDto(instance, assignedCount));
    }

    @GetMapping
    public ResponseEntity<Page<ShiftInstanceResponseDto>> search(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        Page<ShiftInstance> page = shiftInstanceService.search(locationId, startDate, endDate, pageable);
        return ResponseEntity.ok(page.map(instance -> {
            int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
            return shiftInstanceMapper.toDto(instance, assignedCount);
        }));
    }

    @GetMapping("/by-location/{locationId}/date/{date}")
    public ResponseEntity<List<ShiftInstanceResponseDto>> findByLocationAndDate(
            @PathVariable Long locationId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ShiftInstance> list = shiftInstanceService.findByLocationAndDate(locationId, date);
        return ResponseEntity.ok(list.stream().map(instance -> {
            int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
            return shiftInstanceMapper.toDto(instance, assignedCount);
        }).toList());
    }

    @GetMapping("/by-location/{locationId}/range")
    public ResponseEntity<List<ShiftInstanceResponseDto>> findByLocationAndDateRange(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ShiftInstance> list = shiftInstanceService.findByLocationAndDateRange(locationId, startDate, endDate);
        return ResponseEntity.ok(list.stream().map(instance -> {
            int assignedCount = shiftInstanceRepository.countActiveAssignments(instance.getId());
            return shiftInstanceMapper.toDto(instance, assignedCount);
        }).toList());
    }
}
