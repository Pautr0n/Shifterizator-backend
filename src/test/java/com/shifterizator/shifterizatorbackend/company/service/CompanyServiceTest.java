package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import com.shifterizator.shifterizatorbackend.company.service.domain.CompanyDomainService;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private LocationService locationService;

    @Mock
    private CompanyDomainService companyDomainService;

    @InjectMocks
    private CompanyService companyService;


    private final CompanyRequestDto REQUEST_DTO = new CompanyRequestDto("Company 1"
            , "Legal Company 1"
            , "12345678A"
            , "company1@company.com"
            , "+341111111"
            , "ES");

    private final CompanyRequestDto UPDATE_REQUEST = new CompanyRequestDto("Company 4"
            , "Legal Company 4"
            , "444444444A"
            , "company4@company.com"
            , "+344444444444"
            , "ES");

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
        doNothing().when(companyDomainService).validateUniqueName(any());
        doNothing().when(companyDomainService).validateUniqueTaxId(any());
        doNothing().when(companyDomainService).validateUniqueEmail(any());
        when(companyMapper.toEntity(any())).thenReturn(company1);
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.createCompany(REQUEST_DTO);

        assertEquals("Company 1", result.getName());
        verify(companyDomainService).validateUniqueName("Company 1");
        verify(companyDomainService).validateUniqueTaxId("12345678A");
        verify(companyDomainService).validateUniqueEmail("company1@company.com");
        verify(companyRepository).save(company1);
    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_name_exist() {
        doThrow(new CompanyValidationException("Company name already exists: Company 1"))
                .when(companyDomainService).validateUniqueName("Company 1");

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company name already exists: Company 1", exception.getMessage());
        verify(companyDomainService).validateUniqueName("Company 1");
    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_taxId_exist() {
        doNothing().when(companyDomainService).validateUniqueName(any());
        doThrow(new CompanyValidationException("Company tax id already exists: 12345678A"))
                .when(companyDomainService).validateUniqueTaxId("12345678A");

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company tax id already exists: 12345678A", exception.getMessage());
        verify(companyDomainService).validateUniqueName("Company 1");
        verify(companyDomainService).validateUniqueTaxId("12345678A");
    }

    @Test
    void createCompany_should_throw_CompanyValidationException_if_email_exist() {
        doNothing().when(companyDomainService).validateUniqueName(any());
        doNothing().when(companyDomainService).validateUniqueTaxId(any());
        doThrow(new CompanyValidationException("Company email already exists: company1@company.com"))
                .when(companyDomainService).validateUniqueEmail("company1@company.com");

        Exception exception = assertThrows(CompanyValidationException.class
                , () -> companyService.createCompany(REQUEST_DTO));

        assertEquals("Company email already exists: company1@company.com", exception.getMessage());
        verify(companyDomainService).validateUniqueName("Company 1");
        verify(companyDomainService).validateUniqueTaxId("12345678A");
        verify(companyDomainService).validateUniqueEmail("company1@company.com");
    }

    @Test
    void updateCompany_should_return_updated_company_if_validations_ok() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        doNothing().when(companyDomainService).validateUpdateConstraints(any(), any());
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.updateCompany(1L, UPDATE_REQUEST);

        assertEquals("Company 4", result.getName());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);
        verify(companyRepository).save(company1);
    }

    @Test
    void updateCompany_should_throw_CompanyValidationException_if_name_exists() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        doThrow(new CompanyValidationException("Company name already exists: Company 4"))
                .when(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company name already exists: Company 4", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);
    }


    @Test
    void updateCompany_should_throw_CompanyValidationException_if_email_exists() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        doThrow(new CompanyValidationException("Company email already exists: company4@company.com"))
                .when(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company email already exists: company4@company.com", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);
    }


    @Test
    void updateCompany_should_throw_CompanyValidationException_if_taxId_exists() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        doThrow(new CompanyValidationException("Company taxId already exists: 444444444A"))
                .when(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);

        Exception exception = assertThrows(CompanyValidationException.class,
                () -> companyService.updateCompany(1L, UPDATE_REQUEST));

        assertEquals("Company taxId already exists: 444444444A", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyDomainService).validateUpdateConstraints(UPDATE_REQUEST, company1);
    }

    @Test
    void updateCompany_should_throw_CompanyNotFoundException_if_company_not_exist() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 1"));

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.updateCompany(1L, REQUEST_DTO));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }

    @Test
    void deactivateCompany_should_set_isActive_false() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        when(companyRepository.save(any())).thenReturn(company1);

        Company result = companyService.deactivateCompany(1L);

        assertFalse(result.getIsActive());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyRepository).save(company1);
    }

    @Test
    void deactivateCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 1"));

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.deactivateCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }


    @Test
    void activateCompany_should_set_isActive_true() {
        when(companyDomainService.validateCompanyExistsAndReturn(3L)).thenReturn(company3);
        when(companyRepository.save(any())).thenReturn(company3);

        Company result = companyService.activateCompany(3L);

        assertTrue(result.getIsActive());
        verify(companyDomainService).validateCompanyExistsAndReturn(3L);
        verify(companyRepository).save(company3);
    }

    @Test
    void activateCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 1"));

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.activateCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }

    @Test
    void deleteCompany_should_soft_delete_if_exists() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);
        when(companyRepository.save(any())).thenReturn(company1);

        companyService.deleteCompany(1L);

        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
        verify(companyRepository).save(company1);
        assertNotNull(company1.getDeletedAt());
    }

    @Test
    void deleteCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 1"));

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.deleteCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }

    @Test
    void getCompany_should_return_company_if_exists() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L)).thenReturn(company1);

        Company result = companyService.getCompany(1L);

        assertEquals("Company 1", result.getName());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }

    @Test
    void getCompany_should_throw_CompanyNotFoundException_if_not_exist() {
        when(companyDomainService.validateCompanyExistsAndReturn(1L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 1"));

        Exception exception = assertThrows(CompanyNotFoundException.class,
                () -> companyService.getCompany(1L));

        assertEquals("Company not found with id: 1", exception.getMessage());
        verify(companyDomainService).validateCompanyExistsAndReturn(1L);
    }


    @Test
    void search_should_return_page_with_no_filters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(companyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(company1, company2, company3), pageable, 3));

        Page<Company> result = companyService.search(null, null, null, null, null, pageable);

        assertEquals(3, result.getContent().size());
        verify(companyRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void search_should_filter_by_name() {
        Pageable pageable = PageRequest.of(0, 10);
        when(companyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(company1, company2), pageable, 2));

        Page<Company> result = companyService.search("Comp", null, null, null, null, pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_should_filter_by_isActive_true() {
        Pageable pageable = PageRequest.of(0, 10);
        when(companyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(company1, company2), pageable, 2));

        Page<Company> result = companyService.search(null, null, null, null, true, pageable);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void search_should_filter_by_isActive_false() {
        Pageable pageable = PageRequest.of(0, 10);
        when(companyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(company3), pageable, 1));

        Page<Company> result = companyService.search(null, null, null, null, false, pageable);

        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getIsActive());
    }

    @Test
    void search_should_filter_by_name_and_isActive() {
        Pageable pageable = PageRequest.of(0, 10);
        when(companyRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(company1), pageable, 1));

        Page<Company> result = companyService.search("Comp", null, null, null, true, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Company 1", result.getContent().get(0).getName());
        assertTrue(result.getContent().get(0).getIsActive());
    }

}