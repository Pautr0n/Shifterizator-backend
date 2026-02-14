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

    public Company validateCompanyExistsAndReturn(Long id) {
        return companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));
    }

    public void validateUniqueName(String name) {
        if (companyRepository.findByNameAndDeletedAtIsNull(name).isPresent()) {
            throw new CompanyValidationException("Company name already exists: " + name);
        }
    }

    public void validateUniqueTaxId(String taxId) {
        if (companyRepository.findByTaxIdAndDeletedAtIsNull(taxId).isPresent()) {
            throw new CompanyValidationException("Company tax id already exists: " + taxId);
        }
    }

    public void validateUniqueEmail(String email) {
        if (companyRepository.findByEmailAndDeletedAtIsNull(email).isPresent()) {
            throw new CompanyValidationException("Company email already exists: " + email);
        }
    }

    public void validateUpdateConstraints(CompanyRequestDto requestDto, Company company) {
        if (!requestDto.name().equalsIgnoreCase(company.getName())) {
            if (companyRepository.existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(requestDto.name(), company.getId())) {
                throw new CompanyValidationException("Company name already exists: " + requestDto.name());
            }
        }

        if (!requestDto.email().equalsIgnoreCase(company.getEmail())) {
            if (companyRepository.existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(requestDto.email(), company.getId())) {
                throw new CompanyValidationException("Company email already exists: " + requestDto.email());
            }
        }

        if (!requestDto.taxId().equalsIgnoreCase(company.getTaxId())) {
            if (companyRepository.existsByTaxIdIgnoreCaseAndIdNotAndDeletedAtIsNull(requestDto.taxId(), company.getId())) {
                throw new CompanyValidationException("Company taxId already exists: " + requestDto.taxId());
            }
        }
    }
}
