package com.shifterizator.shifterizatorbackend.employee.service.domain;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.exception.UserAlreadyAssignedToEmployeeException;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotInEmployeeCompanyException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeDomainService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final LanguageRepository languageRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final UserRepository userRepository;

    public void validateEmailUniqueness(EmployeeRequestDto dto, Long currentEmployeeId) {

        if (dto.email() == null) return;

        for (Long companyId : dto.companyIds()) {
            boolean exists = employeeRepository.existsByEmailAndCompany(dto.email(), companyId);

            if (exists && !isSameEmployee(currentEmployeeId, dto.email())) {
                throw new EmailAlreadyExistsException("Email already exists for this company");
            }
        }
    }

    private boolean isSameEmployee(Long employeeId, String email) {
        if (employeeId == null) return false;
        return employeeRepository.findById(employeeId)
                .map(e -> email.equals(e.getEmail()))
                .orElse(false);
    }

    public void ensureEmployeeCanBeDeleted(Long employeeId) {
        if (employeeRepository.isEmployeeAssignedToAnyShift(employeeId)) {
            throw new IllegalStateException("Employee is assigned to shifts and cannot be deleted");
        }
    }


    public void assignCompanies(Employee employee, EmployeeRequestDto dto) {

        employee.getEmployeeCompanies().clear();

        for (Long companyId : dto.companyIds()) {
            Company company = companyRepository.findByIdAndDeletedAtIsNull(companyId)
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

            EmployeeCompany ec = EmployeeCompany.builder()
                    .employee(employee)
                    .company(company)
                    .build();

            employee.addCompany(ec);
        }
    }

    public void assignLocations(Employee employee, EmployeeRequestDto dto) {

        employee.getEmployeeLocations().clear();

        if (dto.locationIds() == null) return;

        for (Long locationId : dto.locationIds()) {
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new LocationNotFoundException("Location not found"));

            EmployeeLocation el = EmployeeLocation.builder()
                    .employee(employee)
                    .location(location)
                    .build();

            employee.addLocation(el);
        }
    }

    public void assignLanguages(Employee employee, EmployeeRequestDto dto) {
        employee.getEmployeeLanguages().clear();

        if (dto.languageIds() == null) return;

        for (Long languageId : dto.languageIds()) {
            Language language = languageRepository.findById(languageId)
                    .orElseThrow(() -> new LanguageNotFoundException("Language not found"));

            EmployeeLanguage el = EmployeeLanguage.builder()
                    .employee(employee)
                    .language(language)
                    .build();

            employee.addLanguage(el);
        }
    }

    public void assignShiftPreferences(Employee employee, EmployeeRequestDto dto) {
        assignShiftPreferencesFromIds(employee,
                dto.preferredShiftTemplateIds() == null ? List.of() : dto.preferredShiftTemplateIds());
    }

    /**
     * Clears and reassigns shift preferences from an ordered list of template IDs.
     */
    public void assignShiftPreferencesFromIds(Employee employee, List<Long> templateIds) {
        employee.getShiftPreferences().clear();

        if (templateIds == null || templateIds.isEmpty()) {
            return;
        }

        int order = 1;
        for (Long templateId : templateIds) {
            ShiftTemplate template = shiftTemplateRepository.findById(templateId)
                    .filter(t -> t.getDeletedAt() == null)
                    .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found: " + templateId));

            EmployeeShiftPreference preference = EmployeeShiftPreference.builder()
                    .employee(employee)
                    .shiftTemplate(template)
                    .priorityOrder(order++)
                    .build();

            employee.addShiftPreference(preference);
        }
    }

    /**
     * Assigns a user to an employee if userId is provided in the DTO.
     * Validates that:
     * - User exists
     * - User is not already assigned to another employee (unless it's the same employee being updated)
     *
     * @param employee The employee to assign the user to
     * @param dto The request DTO containing optional userId
     * @param currentEmployeeId The ID of the employee being updated (null for create)
     */
    public void assignUser(Employee employee, EmployeeRequestDto dto, Long currentEmployeeId) {
        if (dto.userId() == null) {
            // If userId is null, remove the user assignment (set to null)
            employee.setUser(null);
            return;
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(dto.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + dto.userId()));

        // User must belong to one of the employee's companies
        if (user.getCompany() == null) {
            throw new UserNotInEmployeeCompanyException("User must belong to a company to be assigned to an employee");
        }
        Set<Long> employeeCompanyIds = dto.companyIds();
        if (!employeeCompanyIds.contains(user.getCompany().getId())) {
            throw new UserNotInEmployeeCompanyException(
                    "User must belong to the same company as the employee. Only users from the employee's companies can be assigned.");
        }

        // Check if user is already assigned to another employee
        employeeRepository.findByUserId(dto.userId())
                .ifPresent(existingEmployee -> {
                    // If updating the same employee, allow it
                    if (!existingEmployee.getId().equals(currentEmployeeId)) {
                        throw new UserAlreadyAssignedToEmployeeException(
                                "This user is already assigned to another employee. Please choose a different user or unassign them from the other employee first."
                        );
                    }
                });

        employee.setUser(user);
    }
}
