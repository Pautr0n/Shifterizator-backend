package com.shifterizator.shifterizatorbackend.employee.controller;

import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.PositionMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;
    private final PositionMapper positionMapper;

    @PostMapping
    public ResponseEntity<PositionDto> create(@RequestParam String name,
                                              @RequestParam Long companyId) {
        Position position = positionService.create(name, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(positionMapper.toDto(position));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PositionDto> update(@PathVariable Long id,
                                              @RequestParam String name) {
        Position position = positionService.update(id, name);
        return ResponseEntity.ok(positionMapper.toDto(position));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PositionDto> findById(@PathVariable Long id) {
        Position position = positionService.findById(id);
        return ResponseEntity.ok(positionMapper.toDto(position));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<PositionDto>> findByCompany(@PathVariable Long companyId) {
        List<Position> positions = positionService.findByCompany(companyId);
        return ResponseEntity.ok(positions.stream().map(positionMapper::toDto).toList());
    }
}