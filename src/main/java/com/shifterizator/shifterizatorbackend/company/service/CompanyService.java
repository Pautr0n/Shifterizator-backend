package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.service.domain.CompanyDomainService;
import com.shifterizator.shifterizatorbackend.company.spec.CompanySpecs;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final EmployeeService employeeService;
    private final LocationService locationService;
    private final CompanyDomainService companyDomainService;


    public Company createCompany(CompanyRequestDto requestDto) {
        companyDomainService.validateUniqueName(requestDto.name());
        companyDomainService.validateUniqueTaxId(requestDto.taxId());
        companyDomainService.validateUniqueEmail(requestDto.email());

        Company newCompany = companyMapper.toEntity(requestDto);
        return companyRepository.save(newCompany);
    }

    @Transactional
    public Company updateCompany(Long id, CompanyRequestDto requestDto) {
        Company company = companyDomainService.validateCompanyExistsAndReturn(id);
        companyDomainService.validateUpdateConstraints(requestDto, company);

        company.setName(requestDto.name());
        company.setLegalName(requestDto.legalName());
        company.setTaxId(requestDto.taxId());
        company.setEmail(requestDto.email());
        company.setPhone(requestDto.phone());
        company.setCountry(requestDto.country());

        return companyRepository.save(company);
    }

    @Transactional
    public Company deactivateCompany(Long id) {
        Company company = companyDomainService.validateCompanyExistsAndReturn(id);
        company.setIsActive(false);
        return companyRepository.save(company);
    }

    @Transactional
    public Company activateCompany(Long id) {
        Company company = companyDomainService.validateCompanyExistsAndReturn(id);
        company.setIsActive(true);
        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyDomainService.validateCompanyExistsAndReturn(id);
        companyRepository.delete(company);
    }

    public Company getCompany(Long id) {
        return companyDomainService.validateCompanyExistsAndReturn(id);
    }

    /**
     * Returns all employees belonging to the given company. Throws if company does not exist.
     */
    public List<Employee> getCompanyEmployees(Long companyId) {
        companyDomainService.validateCompanyExistsAndReturn(companyId);
        return employeeService.search(companyId, null, null, null, Pageable.unpaged()).getContent();
    }

    /**
     * Returns all locations of the given company. Throws if company does not exist.
     */
    public List<Location> getCompanyLocations(Long companyId) {
        companyDomainService.validateCompanyExistsAndReturn(companyId);
        return locationService.findByCompany(companyId);
    }

    public List<Company> listAllCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Paginated search with optional filters: name, country, email, taxId, isActive.
     */
    public Page<Company> search(String name, String country, String email, String taxId, Boolean isActive, Pageable pageable) {
        Specification<Company> spec = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.isBlank()) {
            spec = spec.and(CompanySpecs.nameContains(name));
        }
        if (country != null && !country.isBlank()) {
            spec = spec.and(CompanySpecs.byCountry(country));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(CompanySpecs.byEmail(email));
        }
        if (taxId != null && !taxId.isBlank()) {
            spec = spec.and(CompanySpecs.byTaxId(taxId));
        }
        if (isActive != null) {
            spec = spec.and(CompanySpecs.byIsActive(isActive));
        }

        return companyRepository.findAll(spec, pageable);
    }

    public List<Company> listActiveCompanies() {

        return companyRepository.findByIsActive(true);

    }

    public List<Company> listInActiveCompanies() {

        return companyRepository.findByIsActive(false);

    }

    public List<Company> searchActiveCompaniesByName(String name) {

        return companyRepository.findByNameContainingIgnoreCaseAndIsActive(name, true);

    }

    public List<Company> searchInActiveCompaniesByName(String name) {

        return companyRepository.findByNameContainingIgnoreCaseAndIsActive(name, false);

    }

    public List<Company> searchAllCompaniesByName(String name) {
        return companyRepository.findByNameContainingIgnoreCase(name);
    }
}

