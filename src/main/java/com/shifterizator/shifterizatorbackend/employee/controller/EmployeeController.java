package com.shifterizator.shifterizatorbackend.employee.controller;


import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {


    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto dto) {

        Employee employee = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeMapper.toResponse(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        Employee employee = employeeService.update(id, dto);
        return ResponseEntity.ok((employeeMapper.toResponse(employee)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean hardDelete) {
        employeeService.delete(id, hardDelete);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> findById(@PathVariable Long id) {

        Employee employee = employeeService.findById(id);

        return ResponseEntity.ok(employeeMapper.toResponse(employee));

    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> search(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            Pageable pageable
    ) {
        Page<EmployeeResponseDto> result = employeeService.search(companyId,
                        locationId,
                        name,
                        position,
                        pageable)
                .map(employeeMapper::toResponse);
        return ResponseEntity.ok(result);
    }
}
