package com.shifterizator.shifterizatorbackend.employee.service;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.employee.service.domain.EmployeeDomainService;
import com.shifterizator.shifterizatorbackend.employee.spec.EmployeeSpecs;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeDomainService employeeDomainService;

    @Override
    public EmployeeResponseDto create(EmployeeRequestDto dto) {

        Position position = positionRepository.findById(dto.positionId())
                .orElseThrow(() -> new EntityNotFoundException("Position not found"));

        employeeDomainService.validateEmailUniqueness(dto, null);

        Employee employee = employeeMapper.toEntity(dto, position);
        employeeRepository.save(employee);

        employeeDomainService.assignCompanies(employee, dto);
        employeeDomainService.assignLocations(employee, dto);

        return employeeMapper.toResponse(employee);
    }

    @Override
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {

        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Position position = positionRepository.findById(dto.positionId())
                .orElseThrow(() -> new EntityNotFoundException("Position not found"));

        employeeDomainService.validateEmailUniqueness(dto, id);

        employee.setName(dto.name());
        employee.setSurname(dto.surname());
        employee.setEmail(dto.email());
        employee.setPhone(dto.phone());
        employee.setPosition(position);

        employeeDomainService.assignCompanies(employee, dto);
        employeeDomainService.assignLocations(employee, dto);

        return employeeMapper.toResponse(employee);
    }

    @Override
    public void delete(Long id, boolean hardDelete) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        employeeDomainService.ensureEmployeeCanBeDeleted(id);

        if (hardDelete) {
            employeeRepository.delete(employee);
        } else {
            employee.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    public EmployeeResponseDto findById(Long id) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        return employeeMapper.toResponse(employee);
    }

    @Override
    public Page<EmployeeResponseDto> search(
            Long companyId,
            Long locationId,
            String nameContains,
            String position,
            Pageable pageable
    ) {
        Specification<Employee> spec = Specification.where(EmployeeSpecs.onlyActive());

        if (companyId != null) {
            spec = spec.and(EmployeeSpecs.byCompany(companyId));
        }

        if (locationId != null) {
            spec = spec.and(EmployeeSpecs.byLocation(locationId));
        }

        if (nameContains != null && !nameContains.isBlank()) {
            spec = spec.and(EmployeeSpecs.nameContains(nameContains));
        }

        if (position != null && !position.isBlank()) {
            spec = spec.and(EmployeeSpecs.byPosition(position));
        }

        return employeeRepository.findAll(spec, pageable)
                .map(employeeMapper::toResponse);

    }
}

