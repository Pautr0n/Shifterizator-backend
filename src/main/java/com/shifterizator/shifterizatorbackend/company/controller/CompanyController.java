package com.shifterizator.shifterizatorbackend.company.controller;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    private final EmployeeMapper employeeMapper;
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<CompanyResponseDto> createCompany(@Valid @RequestBody CompanyRequestDto requestDto) {
        Company company = companyService.createCompany(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyMapper.toDto(company));
    }

    /**
     * Paginated list with optional filters: name, country, email, taxId, isActive.
     */
    @GetMapping
    public ResponseEntity<Page<CompanyResponseDto>> listCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        Page<Company> page = companyService.search(name, country, email, taxId, isActive, pageable);
        return ResponseEntity.ok(page.map(companyMapper::toDto));
    }

    @GetMapping("/isactive")
    public ResponseEntity<List<CompanyResponseDto>> listCompaniesByActive(@RequestParam boolean active) {
        List<Company> companies = active ? companyService.listActiveCompanies() : companyService.listInActiveCompanies();
        return ResponseEntity.ok(companies.stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CompanyResponseDto>> listActiveCompanies() {
        return ResponseEntity.ok(companyService.listActiveCompanies().stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<CompanyResponseDto>> listInactiveCompanies() {
        return ResponseEntity.ok(companyService.listInActiveCompanies().stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/active/search")
    public ResponseEntity<List<CompanyResponseDto>> searchActiveCompanies(@RequestParam String name) {
        return ResponseEntity.ok(companyService.searchActiveCompaniesByName(name).stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/inactive/search")
    public ResponseEntity<List<CompanyResponseDto>> searchInactiveCompanies(@RequestParam String name) {
        return ResponseEntity.ok(companyService.searchInActiveCompaniesByName(name).stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponseDto>> searchCompaniesByName(@RequestParam String name) {
        return ResponseEntity.ok(companyService.searchAllCompaniesByName(name).stream().map(companyMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> getCompany(@PathVariable Long id) {
        Company company = companyService.getCompany(id);
        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @GetMapping("/{id}/employees")
    public ResponseEntity<List<EmployeeResponseDto>> getCompanyEmployees(@PathVariable Long id) {
        List<Employee> employees = companyService.getCompanyEmployees(id);
        return ResponseEntity.ok(employees.stream().map(employeeMapper::toResponse).toList());
    }

    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationResponseDto>> getCompanyLocations(@PathVariable Long id) {
        List<Location> locations = companyService.getCompanyLocations(id);
        return ResponseEntity.ok(locations.stream().map(locationMapper::toDto).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDto requestDto
    ) {

        Company company = companyService.updateCompany(id, requestDto);

        return ResponseEntity.ok(companyMapper.toDto(company));

    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CompanyResponseDto> activateCompany(@PathVariable Long id) {

        Company company = companyService.activateCompany(id);

        return ResponseEntity.ok(companyMapper.toDto(company));

    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CompanyResponseDto> deactivateCompany(@PathVariable Long id) {
        Company company = companyService.deactivateCompany(id);
        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {

        companyService.deleteCompany(id);

        return ResponseEntity.noContent().build();

    }

}
