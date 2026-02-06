package com.shifterizator.shifterizatorbackend.employee.service.domain;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeCompany;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLocation;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeDomainServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private EmployeeDomainService service;

    @Test
    void validateEmailUniqueness_shouldDoNothingWhenEmailIsNull() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", null, "123", 1L, Set.of(1L), null
        );

        service.validateEmailUniqueness(dto, null);

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void validateEmailUniqueness_shouldThrowWhenEmailExistsForAnotherEmployee() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null
        );

        when(employeeRepository.existsByEmailAndCompany("john@example.com", 1L))
                .thenReturn(true);
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.of(Employee.builder().id(99L).email("other@example.com").build()));

        assertThatThrownBy(() -> service.validateEmailUniqueness(dto, 99L))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists for this company");
    }

    @Test
    void validateEmailUniqueness_shouldNotThrowWhenSameEmployeeKeepsSameEmail() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null
        );

        when(employeeRepository.existsByEmailAndCompany("john@example.com", 1L))
                .thenReturn(true);
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.of(Employee.builder().id(99L).email("john@example.com").build()));

        assertThatCode(() -> service.validateEmailUniqueness(dto, 99L))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureEmployeeCanBeDeleted_shouldThrowWhenAssignedToShifts() {
        when(employeeRepository.isEmployeeAssignedToAnyShift(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.ensureEmployeeCanBeDeleted(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Employee is assigned to shifts and cannot be deleted");
    }

    @Test
    void ensureEmployeeCanBeDeleted_shouldDoNothingWhenNotAssignedToShifts() {
        when(employeeRepository.isEmployeeAssignedToAnyShift(1L)).thenReturn(false);

        assertThatCode(() -> service.ensureEmployeeCanBeDeleted(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void assignCompanies_shouldClearAndAssignNewOnes() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeCompanies().add(EmployeeCompany.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L, 2L), null
        );

        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");
        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Cyberdyne");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company2));

        service.assignCompanies(employee, dto);

        assertThat(employee.getEmployeeCompanies()).hasSize(2);
        assertThat(employee.getEmployeeCompanies())
                .extracting(ec -> ec.getCompany().getId())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void assignCompanies_shouldThrowWhenCompanyNotFound() {
        Employee employee = Employee.builder().build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null
        );

        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCompanies(employee, dto))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found");
    }

    @Test
    void assignLocations_shouldClearAndAssignNewOnes() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeLocations().add(EmployeeLocation.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), Set.of(10L, 11L)
        );

        Location loc1 = Location.builder().id(10L).name("HQ").build();
        Location loc2 = Location.builder().id(11L).name("Branch").build();

        when(locationRepository.findById(10L)).thenReturn(Optional.of(loc1));
        when(locationRepository.findById(11L)).thenReturn(Optional.of(loc2));

        service.assignLocations(employee, dto);

        assertThat(employee.getEmployeeLocations()).hasSize(2);
        assertThat(employee.getEmployeeLocations())
                .extracting(el -> el.getLocation().getId())
                .containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void assignLocations_shouldDoNothingWhenLocationIdsNull() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeLocations().add(EmployeeLocation.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null
        );

        service.assignLocations(employee, dto);

        assertThat(employee.getEmployeeLocations()).isEmpty();
    }

    @Test
    void assignLocations_shouldThrowWhenLocationNotFound() {
        Employee employee = Employee.builder().build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), Set.of(10L)
        );

        when(locationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignLocations(employee, dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }
}
