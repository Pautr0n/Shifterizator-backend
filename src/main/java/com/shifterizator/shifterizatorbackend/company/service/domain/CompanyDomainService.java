package com.shifterizator.shifterizatorbackend.company.service.domain;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyDomainService {

    private final CompanyRepository companyRepository;

    /**
     * Validates that a company exists and returns it.
     *
     * @param id the company ID
     * @return the Company entity
     * @throws CompanyNotFoundException if company is not found
     */
    public Company validateCompanyExistsAndReturn(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));
    }

    /**
     * Validates that company name is unique.
     *
     * @param name the company name to validate
     * @throws CompanyValidationException if name already exists
     */
    public void validateUniqueName(String name) {
        if (companyRepository.findByName(name).isPresent()) {
            throw new CompanyValidationException("Company name already exists: " + name);
        }
    }

    /**
     * Validates that company tax ID is unique.
     *
     * @param taxId the tax ID to validate
     * @throws CompanyValidationException if tax ID already exists
     */
    public void validateUniqueTaxId(String taxId) {
        if (companyRepository.findByTaxId(taxId).isPresent()) {
            throw new CompanyValidationException("Company tax id already exists: " + taxId);
        }
    }

    /**
     * Validates that company email is unique.
     *
     * @param email the email to validate
     * @throws CompanyValidationException if email already exists
     */
    public void validateUniqueEmail(String email) {
        if (companyRepository.findByEmail(email).isPresent()) {
            throw new CompanyValidationException("Company email already exists: " + email);
        }
    }

    /**
     * Validates update constraints for company uniqueness.
     * Checks if changed fields (name, email, taxId) are unique when different from current values.
     *
     * @param requestDto the update request DTO
     * @param company the existing company entity
     * @throws CompanyValidationException if any unique constraint would be violated
     */
    public void validateUpdateConstraints(CompanyRequestDto requestDto, Company company) {
        if (!requestDto.name().equalsIgnoreCase(company.getName())) {
            if (companyRepository.existsByNameIgnoreCaseAndIdNot(requestDto.name(), company.getId())) {
                throw new CompanyValidationException("Company name already exists: " + requestDto.name());
            }
        }

        if (!requestDto.email().equalsIgnoreCase(company.getEmail())) {
            if (companyRepository.existsByEmailIgnoreCaseAndIdNot(requestDto.email(), company.getId())) {
                throw new CompanyValidationException("Company email already exists: " + requestDto.email());
            }
        }

        if (!requestDto.taxId().equalsIgnoreCase(company.getTaxId())) {
            if (companyRepository.existsByTaxIdIgnoreCaseAndIdNot(requestDto.taxId(), company.getId())) {
                throw new CompanyValidationException("Company taxId already exists: " + requestDto.taxId());
            }
        }
    }
}
