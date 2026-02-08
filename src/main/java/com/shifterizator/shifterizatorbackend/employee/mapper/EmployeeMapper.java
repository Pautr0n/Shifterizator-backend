package com.shifterizator.shifterizatorbackend.employee.mapper;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeCompany;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLocation;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequestDto dto, Position position) {
        return Employee.builder()
                .name(dto.name())
                .surname(dto.surname())
                .email(dto.email())
                .phone(dto.phone())
                .position(position)
                .build();
    }

    public EmployeeResponseDto toResponse(Employee employee) {
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getName(),
                employee.getSurname(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getPosition().getName(),
                extractCompanyNames(employee.getEmployeeCompanies()),
                extractLocationNames(employee.getEmployeeLocations()),
                extractLanguageNames(employee.getEmployeeLanguages()),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    private Set<String> extractCompanyNames(Set<EmployeeCompany> employeeCompanies) {
        return employeeCompanies.stream()
                .map(ec -> ec.getCompany().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> extractLocationNames(Set<EmployeeLocation> employeeLocations) {
        return employeeLocations.stream()
                .map(el -> el.getLocation().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> extractLanguageNames(Set<EmployeeLanguage> employeeLanguages) {
        return employeeLanguages.stream()
                .map(el -> el.getLanguage().getName())
                .collect(Collectors.toSet());
    }
}
