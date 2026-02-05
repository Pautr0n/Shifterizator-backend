package com.shifterizator.shifterizatorbackend.employee.controller;


import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public EmployeeResponseDto create(@Valid @RequestBody EmployeeRequestDto dto) {
        return employeeService.create(dto);
    }

    @PutMapping("/{id}")
    public EmployeeResponseDto update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        return employeeService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        employeeService.delete(id, hardDelete);
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto findById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @GetMapping
    public Page<EmployeeResponseDto> search(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            Pageable pageable
    ) {
        return employeeService.search(companyId, locationId, name, position, pageable);
    }
}
