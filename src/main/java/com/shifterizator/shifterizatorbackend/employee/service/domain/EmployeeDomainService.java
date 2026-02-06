package com.shifterizator.shifterizatorbackend.employee.service.domain;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeDomainService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;

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
            Company company = companyRepository.findById(companyId)
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
}
