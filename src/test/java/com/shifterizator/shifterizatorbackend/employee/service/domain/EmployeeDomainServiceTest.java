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
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLocation;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;

    @InjectMocks
    private EmployeeDomainService service;

    @Test
    void validateEmailUniqueness_shouldDoNothingWhenEmailIsNull() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", null, "123", 1L, Set.of(1L), null, null, null, null
        );

        service.validateEmailUniqueness(dto, null);

        verifyNoInteractions(employeeRepository);
    }

    @Test
    void validateEmailUniqueness_shouldThrowWhenEmailExistsForAnotherEmployee() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, null
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
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, null
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
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L, 2L), null, null, null, null
        );

        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");
        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Cyberdyne");

        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(company2));

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
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, null
        );

        when(companyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignCompanies(employee, dto))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found");
    }

    @Test
    void assignLocations_shouldClearAndAssignNewOnes() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeLocations().add(EmployeeLocation.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), Set.of(10L, 11L), null, null, null
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
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, null
        );

        service.assignLocations(employee, dto);

        assertThat(employee.getEmployeeLocations()).isEmpty();
    }

    @Test
    void assignLocations_shouldThrowWhenLocationNotFound() {
        Employee employee = Employee.builder().build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), Set.of(10L), null, null, null
        );

        when(locationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignLocations(employee, dto))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");
    }

    @Test
    void assignLanguages_shouldClearAndAssignNewOnes() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeLanguages().add(EmployeeLanguage.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, Set.of(1L, 2L), null, null
        );

        Language lang1 = Language.builder().id(1L).code("EN").name("English").build();
        Language lang2 = Language.builder().id(2L).code("ES").name("Spanish").build();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(lang1));
        when(languageRepository.findById(2L)).thenReturn(Optional.of(lang2));

        service.assignLanguages(employee, dto);

        assertThat(employee.getEmployeeLanguages()).hasSize(2);
        assertThat(employee.getEmployeeLanguages())
                .extracting(el -> el.getLanguage().getId())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void assignLanguages_shouldDoNothingWhenLanguageIdsNull() {
        Employee employee = Employee.builder().build();
        employee.getEmployeeLanguages().add(EmployeeLanguage.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, null
        );

        service.assignLanguages(employee, dto);

        assertThat(employee.getEmployeeLanguages()).isEmpty();
    }

    @Test
    void assignLanguages_shouldThrowWhenLanguageNotFound() {
        Employee employee = Employee.builder().build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, Set.of(1L), null, null
        );

        when(languageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignLanguages(employee, dto))
                .isInstanceOf(LanguageNotFoundException.class)
                .hasMessage("Language not found");
    }

    @Test
    void assignShiftPreferences_shouldClearAndAssignFromDto() {
        Employee employee = Employee.builder().build();
        employee.getShiftPreferences().add(com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference.builder().id(99L).build());

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, List.of(10L, 20L)
        );

        ShiftTemplate t1 = ShiftTemplate.builder().id(10L).build();
        ShiftTemplate t2 = ShiftTemplate.builder().id(20L).build();

        when(shiftTemplateRepository.findById(10L)).thenReturn(Optional.of(t1));
        when(shiftTemplateRepository.findById(20L)).thenReturn(Optional.of(t2));

        service.assignShiftPreferences(employee, dto);

        assertThat(employee.getShiftPreferences()).hasSize(2);
        assertThat(employee.getShiftPreferences())
                .extracting(esp -> esp.getShiftTemplate().getId())
                .containsExactlyInAnyOrder(10L, 20L);
        assertThat(employee.getShiftPreferences())
                .extracting(com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference::getPriorityOrder)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void assignShiftPreferences_shouldThrowWhenTemplateNotFound() {
        Employee employee = Employee.builder().build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123", 1L, Set.of(1L), null, null, null, List.of(10L)
        );

        when(shiftTemplateRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignShiftPreferences(employee, dto))
                .isInstanceOf(ShiftTemplateNotFoundException.class)
                .hasMessageContaining("Shift template not found");
    }
}
