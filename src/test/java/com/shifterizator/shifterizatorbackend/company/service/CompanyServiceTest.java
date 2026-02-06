package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;


    private final CompanyRequestDto REQUEST_DTO = new CompanyRequestDto("Company 1"
            , "Legal Company 1"
            , "12345678A"
            , "company1@company.com"
            , "+341111111");

    private final CompanyRequestDto UPDATE_REQUEST = new CompanyRequestDto("Company 4"
            , "Legal Company 4"
            , "444444444A"
            , "company4@company.com"
            , "+344444444444");

    private Company company1, company2, company3;

    @BeforeEach
    void setup() {
        company1 = new Company("Company 1"
                , "Legal Company 1"
                , "12345678A"
                , "company1@company.com"
                , "+341111111");
        company1.setId(1L);

        company2 = new Company("Company 2"
                , "Legal Company 2"
                , "12345678B"
                , "company2@company.com"
                , "+342222222");
        company2.setId(2L);

        company3 = new Company("Company 3"
                , "Legal Company 3"
                , "12345678C"
                , "company3@company.com"
                , "+343333333");
        company3.setId(3L);
        company3.setIsActive(false);
    }

    @Test
    void createCompany_should_return_company_if_validations_ok() {
        when(companyMapper.toEntity(any())).thenReturn(company1);
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.createCompany(REQUEST_DTO);

        assertEquals("Company 1", result.getName());
        verify(companyRepository).save(company1);

    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_name_exist() {
        when(companyRepository.findByName(any())).thenReturn(Optional.ofNullable(company1));

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company name already exists: Company 1", exception.getMessage());
        verify(companyRepository).findByName("Company 1");

    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_taxId_exist() {
        when(companyRepository.findByTaxId(any()))
                .thenReturn(Optional.ofNullable(company1));

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company tax id already exists: 12345678A", exception.getMessage());
        verify(companyRepository).findByTaxId("12345678A");

    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_email_exist() {
        when(companyRepository.findByEmail(any()))
                .thenReturn(Optional.ofNullable(company1));

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company email already exists: company1@company.com", exception.getMessage());
        verify(companyRepository).findByEmail("company1@company.com");

    }

    @Test
    void updateCompany_should_return_updated_company_if_validations_ok() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("Company 4", 1L)).thenReturn(false);
        when(companyRepository.existsByEmailIgnoreCaseAndIdNot("company4@company.com", 1L)).thenReturn(false);
        when(companyRepository.existsByTaxIdIgnoreCaseAndIdNot("444444444A", 1L)).thenReturn(false);
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.updateCompany(1L, UPDATE_REQUEST);

        assertEquals("Company 4", result.getName());
        verify(companyRepository).findById(1L);
        verify(companyRepository).save(company1);
    }

    @Test
    void updateCompany_should_throw_CompanyValidationException_if_name_exists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.existsByNameIgnoreCaseAndIdNot("Company 4", 1L)).thenReturn(true);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company name already exists: Company 4", exception.getMessage());
        verify(companyRepository).existsByNameIgnoreCaseAndIdNot("Company 4", 1L);
    }


    @Test
    void updateCompany_should_throw_CompanyValidationException_if_email_exists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.existsByEmailIgnoreCaseAndIdNot("company4@company.com", 1L)).thenReturn(true);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company email already exists: company4@company.com", exception.getMessage());
        verify(companyRepository).existsByEmailIgnoreCaseAndIdNot("company4@company.com", 1L);
    }


    @Test
    void updateCompany_should_throw_CompanyValidationException_if_taxId_exists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.existsByTaxIdIgnoreCaseAndIdNot("444444444A", 1L)).thenReturn(true);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company taxId already exists: 444444444A", exception.getMessage());
        verify(companyRepository).existsByTaxIdIgnoreCaseAndIdNot("444444444A", 1L);
    }

    @Test
    void updateCompany_should_throw_CompanyNotFoundException_if_company_not_exist() {
        when(companyRepository.findById(1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.updateCompany(1L, REQUEST_DTO));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    @Test
    void deactivateCompany_should_set_isActive_false() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.deactivateCompany(1L);

        assertFalse(result.getIsActive());
        verify(companyRepository).findById(1L);
        verify(companyRepository).save(company1);
    }

    @Test
    void deactivateCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.deactivateCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyRepository).findById(1L);
    }


    @Test
    void activateCompany_should_set_isActive_true() {
        when(companyRepository.findById(3L)).thenReturn(Optional.of(company3));
        when(companyRepository.save(any())).thenReturn(company3);

        Company result = companyService.activateCompany(3L);

        assertTrue(result.getIsActive());
        verify(companyRepository).findById(3L);
        verify(companyRepository).save(company3);
    }

    @Test
    void activateCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.activateCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    @Test
    void deleteCompany_should_delete_if_exists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));

        companyService.deleteCompany(1L);

        verify(companyRepository).findById(1L);
        verify(companyRepository).delete(company1);
    }

    @Test
    void deleteCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.deleteCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyRepository).findById(1L);
    }

    @Test
    void getCompany_should_return_company_if_exists() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company1));

        Company result = companyService.getCompany(1L);

        assertEquals("Company 1", result.getName());
        verify(companyRepository).findById(1L);
    }

    @Test
    void getCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.getCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyRepository).findById(1L);
    }


    @Test
    void listAllCompanies_should_return_all_companies() {
        when(companyRepository.findAll()).thenReturn(List.of(company1, company2, company3));

        List<Company> result = companyService.listAllCompanies();

        assertEquals(3, result.size());
        verify(companyRepository).findAll();
    }

    @Test
    void listActiveCompanies_should_return_only_active_companies() {
        when(companyRepository.findByIsActive(true)).thenReturn(List.of(company1, company2));

        List<Company> result = companyService.listActiveCompanies();

        assertEquals(2, result.size());
        verify(companyRepository).findByIsActive(true);
    }


    @Test
    void listInActiveCompanies_should_return_only_inactive_companies() {
        when(companyRepository.findByIsActive(false)).thenReturn(List.of(company3));

        List<Company> result = companyService.listInActiveCompanies();

        assertEquals(1, result.size());
        verify(companyRepository).findByIsActive(false);
    }


    @Test
    void searchActiveCompaniesByName_should_return_matches() {
        when(companyRepository.findByNameContainingIgnoreCaseAndIsActive("Comp", true))
                .thenReturn(List.of(company1, company2));

        List<Company> result = companyService.searchActiveCompaniesByName("Comp");

        assertEquals(2, result.size());
        verify(companyRepository).findByNameContainingIgnoreCaseAndIsActive("Comp", true);
    }


    @Test
    void searchInActiveCompaniesByName_should_return_matches() {
        when(companyRepository.findByNameContainingIgnoreCaseAndIsActive("Comp", false))
                .thenReturn(List.of(company3));

        List<Company> result = companyService.searchInActiveCompaniesByName("Comp");

        assertEquals(1, result.size());
        verify(companyRepository).findByNameContainingIgnoreCaseAndIsActive("Comp", false);
    }


    @Test
    void searchAllCompaniesByName_should_return_matches() {
        when(companyRepository.findByNameContainingIgnoreCase("Comp"))
                .thenReturn(List.of(company1, company2, company3));

        List<Company> result = companyService.searchAllCompaniesByName("Comp");

        assertEquals(3, result.size());
        verify(companyRepository).findByNameContainingIgnoreCase("Comp");
    }

}