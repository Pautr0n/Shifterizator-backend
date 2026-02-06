package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    Employee create(EmployeeRequestDto dto);

    Employee update(Long id, EmployeeRequestDto dto);

    void delete(Long id, boolean hardDelete);

    Employee findById(Long id);

    Page<Employee> search(
            Long companyId,
            Long locationId,
            String nameContains,
            String position,
            Pageable pageable
    );

}
