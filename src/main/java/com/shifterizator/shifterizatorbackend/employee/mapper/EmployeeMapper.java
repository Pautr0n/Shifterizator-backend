package com.shifterizator.shifterizatorbackend.employee.mapper;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Comparator;
import java.util.List;
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
                .preferredDayOff(parsePreferredDayOff(dto.preferredDayOff()))
                .shiftsPerWeek(dto.shiftsPerWeek())
                .position(position)
                .build();
    }

    /**
     * Parses preferred day off from DTO string (e.g. "WEDNESDAY"). Returns null if null or blank.
     *
     * @throws IllegalArgumentException if the value is not a valid DayOfWeek name
     */
    public static DayOfWeek parsePreferredDayOff(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid preferredDayOff: '" + value + "'. Must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
        }
    }

    public EmployeeResponseDto toResponse(Employee employee) {
        return new EmployeeResponseDto(
                employee.getId(),
                employee.getName(),
                employee.getSurname(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getPosition() != null ? employee.getPosition().getId() : null,
                employee.getPosition() != null ? employee.getPosition().getName() : null,
                extractCompanyIds(employee.getEmployeeCompanies()),
                extractCompanyNames(employee.getEmployeeCompanies()),
                extractLocationNames(employee.getEmployeeLocations()),
                extractLanguageNames(employee.getEmployeeLanguages()),
                employee.getPreferredDayOff() != null ? employee.getPreferredDayOff().name() : null,
                employee.getShiftsPerWeek(),
                extractPreferredShiftTemplateIds(employee.getShiftPreferences()),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getUser() != null ? employee.getUser().getId() : null,
                employee.getUser() != null ? employee.getUser().getUsername() : null
        );
    }

    private List<Long> extractPreferredShiftTemplateIds(Set<EmployeeShiftPreference> shiftPreferences) {
        if (shiftPreferences == null || shiftPreferences.isEmpty()) {
            return List.of();
        }
        return shiftPreferences.stream()
                .sorted(Comparator.comparing(EmployeeShiftPreference::getPriorityOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(esp -> esp.getShiftTemplate().getId())
                .collect(Collectors.toList());
    }

    public EmployeePreferencesResponseDto toPreferencesResponse(Employee employee) {
        return new EmployeePreferencesResponseDto(
                employee.getPreferredDayOff() != null ? employee.getPreferredDayOff().name() : null,
                extractPreferredShiftTemplateIds(employee.getShiftPreferences()),
                employee.getShiftsPerWeek()
        );
    }

    private Set<Long> extractCompanyIds(Set<EmployeeCompany> employeeCompanies) {
        if (employeeCompanies == null || employeeCompanies.isEmpty()) {
            return Set.of();
        }
        return employeeCompanies.stream()
                .map(ec -> ec.getCompany().getId())
                .collect(Collectors.toSet());
    }

    private Set<String> extractCompanyNames(Set<EmployeeCompany> employeeCompanies) {
        if (employeeCompanies == null || employeeCompanies.isEmpty()) {
            return Set.of();
        }
        return employeeCompanies.stream()
                .map(ec -> ec.getCompany().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> extractLocationNames(Set<EmployeeLocation> employeeLocations) {
        if (employeeLocations == null || employeeLocations.isEmpty()) {
            return Set.of();
        }
        return employeeLocations.stream()
                .map(el -> el.getLocation().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> extractLanguageNames(Set<EmployeeLanguage> employeeLanguages) {
        if (employeeLanguages == null || employeeLanguages.isEmpty()) {
            return Set.of();
        }
        return employeeLanguages.stream()
                .map(el -> el.getLanguage().getName())
                .collect(Collectors.toSet());
    }
}
