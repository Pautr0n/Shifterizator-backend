package com.shifterizator.shifterizatorbackend.company.controller;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public ResponseEntity<CompanyResponseDto> createCompany(@Valid @RequestBody CompanyRequestDto requestDto) {

        Company company = companyService.createCompany(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(companyMapper.toDto(company));

    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> getCompany(@PathVariable Long id) {

        Company company = companyService.getCompany(id);

        return ResponseEntity.ok(companyMapper.toDto(company));

    }

    @GetMapping
    public ResponseEntity<List<CompanyResponseDto>> listAllCompanies() {

        List<Company> companies = companyService.listAllCompanies();

        return ResponseEntity.ok(
                companies.stream().map(companyMapper::toDto).toList()
        );

    }

    @GetMapping("/active")
    public ResponseEntity<List<CompanyResponseDto>> listActiveCompanies() {

        List<Company> companies = companyService.listActiveCompanies();

        return ResponseEntity.ok(
                companies.stream().map(companyMapper::toDto).toList()
        );

    }

    @GetMapping("/inactive")
    public ResponseEntity<List<CompanyResponseDto>> listInactiveCompanies() {

        List<Company> companies = companyService.listInActiveCompanies();

        return ResponseEntity.ok(
                companies.stream().map(companyMapper::toDto).toList()
        );

    }

    @GetMapping("/active/search")
    public ResponseEntity<List<CompanyResponseDto>> searchActiveCompanies(@RequestParam String name) {

        List<Company> companies = companyService.searchActiveCompaniesByName(name);

        return ResponseEntity.ok(companies.stream().map(companyMapper::toDto).toList());

    }

    @GetMapping("/inactive/search")
    public ResponseEntity<List<CompanyResponseDto>> searchInactiveCompanies(@RequestParam String name) {

        List<Company> companies = companyService.searchInActiveCompaniesByName(name);

        return ResponseEntity.ok(companies.stream().map(companyMapper::toDto).toList());

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

    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponseDto>> searchCompaniesByName(@RequestParam String name) {

        List<Company> companies = companyService.searchAllCompaniesByName(name);

        return ResponseEntity.ok(
                companies.stream().map(companyMapper::toDto).toList()
        );

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {

        companyService.deleteCompany(id);

        return ResponseEntity.noContent().build();

    }


}
