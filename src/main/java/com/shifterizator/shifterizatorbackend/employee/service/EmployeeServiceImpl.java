package com.shifterizator.shifterizatorbackend.employee.service;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.employee.service.domain.EmployeeDomainService;
import com.shifterizator.shifterizatorbackend.employee.spec.EmployeeSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeDomainService employeeDomainService;
    private final EmployeeResponseInitializer responseInitializer;

    @Override
    public Employee create(EmployeeRequestDto dto) {

        Position position = positionRepository.findById(dto.positionId())
                .orElseThrow(() -> new PositionNotFoundException("Position not found"));

        employeeDomainService.validateEmailUniqueness(dto, null);

        Employee employee = employeeMapper.toEntity(dto, position);
        employeeDomainService.assignUser(employee, dto, null);

        employeeRepository.save(employee);

        employeeDomainService.assignCompanies(employee, dto);
        employeeDomainService.assignLocations(employee, dto);
        employeeDomainService.assignLanguages(employee, dto);
        employeeDomainService.assignShiftPreferences(employee, dto);

        responseInitializer.initializeForResponse(employee);
        return employee;
    }

    @Override
    public Employee update(Long id, EmployeeRequestDto dto) {

        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Position position = positionRepository.findById(dto.positionId())
                .orElseThrow(() -> new PositionNotFoundException("Position not found"));

        employeeDomainService.validateEmailUniqueness(dto, id);

        employee.setName(dto.name());
        employee.setSurname(dto.surname());
        employee.setEmail(dto.email());
        employee.setPhone(dto.phone());
        employee.setPreferredDayOff(EmployeeMapper.parsePreferredDayOff(dto.preferredDayOff()));
        employee.setShiftsPerWeek(dto.shiftsPerWeek());
        employee.setPosition(position);

        employeeDomainService.assignUser(employee, dto, id);
        employeeDomainService.assignCompanies(employee, dto);
        employeeDomainService.assignLocations(employee, dto);
        employeeDomainService.assignLanguages(employee, dto);
        employeeDomainService.assignShiftPreferences(employee, dto);

        responseInitializer.initializeForResponse(employee);
        return employee;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        employeeDomainService.ensureEmployeeCanBeDeleted(id);

        if (hardDelete) {
            employeeRepository.delete(employee);
        } else {
            employee.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    public Employee findById(Long id) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        responseInitializer.initializeForResponse(employee);
        return employee;
    }

    @Override
    public Page<Employee> search(
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

        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        page.getContent().forEach(responseInitializer::initializeForResponse);
        return page;
    }

    @Override
    public EmployeePreferencesResponseDto getPreferences(Long id) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toPreferencesResponse(employee);
    }

    @Override
    public EmployeePreferencesResponseDto updatePreferences(Long id, EmployeePreferencesRequestDto dto) {
        Employee employee = employeeRepository.findActiveById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        employee.setPreferredDayOff(EmployeeMapper.parsePreferredDayOff(dto.preferredDayOff()));
        employee.setShiftsPerWeek(dto.shiftsPerWeek());
        employeeDomainService.assignShiftPreferencesFromIds(employee,
                dto.preferredShiftTemplateIds() != null ? dto.preferredShiftTemplateIds() : List.of());
        return employeeMapper.toPreferencesResponse(employee);
    }
}

