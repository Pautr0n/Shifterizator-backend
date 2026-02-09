package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-templates")
@RequiredArgsConstructor
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;
    private final ShiftTemplateMapper shiftTemplateMapper;

    @PostMapping
    public ResponseEntity<ShiftTemplateResponseDto> create(@Valid @RequestBody ShiftTemplateRequestDto dto) {
        ShiftTemplate template = shiftTemplateService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftTemplateMapper.toDto(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftTemplateResponseDto> update(@PathVariable Long id, @Valid @RequestBody ShiftTemplateRequestDto dto) {
        ShiftTemplate template = shiftTemplateService.update(id, dto);
        return ResponseEntity.ok(shiftTemplateMapper.toDto(template));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        shiftTemplateService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftTemplateResponseDto> findById(@PathVariable Long id) {
        ShiftTemplate template = shiftTemplateService.findById(id);
        return ResponseEntity.ok(shiftTemplateMapper.toDto(template));
    }

    @GetMapping
    public ResponseEntity<Page<ShiftTemplateResponseDto>> search(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long positionId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                shiftTemplateService.search(locationId, positionId, pageable)
                        .map(shiftTemplateMapper::toDto)
        );
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<ShiftTemplateResponseDto>> findByLocation(@PathVariable Long locationId) {
        List<ShiftTemplate> list = shiftTemplateService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(shiftTemplateMapper::toDto).toList());
    }
}
