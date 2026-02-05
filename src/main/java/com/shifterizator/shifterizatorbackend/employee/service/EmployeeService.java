package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponseDto create(EmployeeRequestDto dto);

    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);

    void delete(Long id, boolean hardDelete);

    EmployeeResponseDto findById(Long id);

    Page<EmployeeResponseDto> search(
            Long companyId,
            Long locationId,
            String nameContains,
            String position,
            Pageable pageable
    );

}
