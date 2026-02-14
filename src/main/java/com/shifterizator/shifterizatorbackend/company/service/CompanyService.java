package com.shifterizator.shifterizatorbackend.company.service;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanyService {

    Company createCompany(CompanyRequestDto requestDto);

    Company updateCompany(Long id, CompanyRequestDto requestDto);

    Company deactivateCompany(Long id);

    Company activateCompany(Long id);

    void deleteCompany(Long id);

    Company getCompany(Long id);

    List<Employee> getCompanyEmployees(Long companyId);

    List<Location> getCompanyLocations(Long companyId);

    Page<Company> search(String name, String country, String email, String taxId, Boolean isActive, Pageable pageable);
}
