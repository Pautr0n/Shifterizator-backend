package com.shifterizator.shifterizatorbackend.employee.controller;

import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public PositionDto create(@RequestParam String name,
                              @RequestParam Long companyId) {
        return positionService.create(name, companyId);
    }

    @PutMapping("/{id}")
    public PositionDto update(@PathVariable Long id,
                              @RequestParam String name) {
        return positionService.update(id, name);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        positionService.delete(id);
    }

    @GetMapping("/{id}")
    public PositionDto findById(@PathVariable Long id) {
        return positionService.findById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<PositionDto> findByCompany(@PathVariable Long companyId) {
        return positionService.findByCompany(companyId);
    }
}