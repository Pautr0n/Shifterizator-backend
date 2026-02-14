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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Companies",
        description = "Endpoints for managing companies (tenants) in the multi-tenant system. " +
                "Company creation and deletion restricted to SUPERADMIN. " +
                "Company activation/deactivation restricted to SUPERADMIN."
)
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMapper companyMapper;
    private final EmployeeMapper employeeMapper;
    private final LocationMapper locationMapper;

    @Operation(
            summary = "Create a new company",
            description = """
                    Creates a new company (tenant) in the system. This is a restricted operation that can only
                    be performed by SUPERADMIN users.
                    
                    **Access:** SUPERADMIN only
                    
                    **Use Case:** Onboarding a new client company to the platform.
                    
                    **Validation:**
                    - Company name, tax ID, and email must be unique
                    - All required fields must be provided and meet validation rules
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Company created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate company data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Company's name must have between 4 and 20 characters"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only SUPERADMIN can create companies"
            )
    })
    @PostMapping
    public ResponseEntity<CompanyResponseDto> createCompany(@Valid @RequestBody CompanyRequestDto requestDto) {
        Company company = companyService.createCompany(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyMapper.toDto(company));
    }

    @Operation(
            summary = "List companies with pagination and filters",
            description = """
                    Retrieves a paginated list of companies with optional filtering capabilities.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Filters:**
                    - `name`: Filter by company name (partial match, case-insensitive)
                    - `country`: Filter by country
                    - `email`: Filter by email (partial match)
                    - `taxId`: Filter by tax ID (partial match)
                    - `isActive`: Filter by active status (true/false)
                    
                    **Pagination:**
                    - `page`: Page number (0-indexed, default: 0)
                    - `size`: Page size (default: 20)
                    - `sort`: Sort criteria (e.g., `name,asc` or `createdAt,desc`)
                    
                    **Use Case:** Browse and search companies. Use e.g. ?isActive=true for active only, ?name=Acme for search by name.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Companies retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            )
    })
    @GetMapping
    public ResponseEntity<Page<CompanyResponseDto>> listCompanies(
            @Parameter(description = "Filter by company name (partial match)", example = "Acme")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Filter by country", example = "Spain")
            @RequestParam(required = false) String country,
            
            @Parameter(description = "Filter by email (partial match)", example = "@acme")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "Filter by tax ID (partial match)", example = "B123")
            @RequestParam(required = false) String taxId,
            
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean isActive,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        Page<Company> page = companyService.search(name, country, email, taxId, isActive, pageable);
        return ResponseEntity.ok(page.map(companyMapper::toDto));
    }

    @Operation(
            summary = "Get company by ID",
            description = """
                    Retrieves detailed information about a specific company by its ID.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Use Case:** View company details, edit company information.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Company retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "NOT_FOUND",
                                      "message": "Company not found with id: 999"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> getCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        Company company = companyService.getCompany(id);
        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @Operation(
            summary = "Get all employees of a company",
            description = """
                    Retrieves a list of all employees belonging to a specific company.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, READONLYMANAGER, EMPLOYEE
                    
                    **Use Case:** View all employees in a company for management or reporting purposes.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employees retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @GetMapping("/{id}/employees")
    public ResponseEntity<List<EmployeeResponseDto>> getCompanyEmployees(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        List<Employee> employees = companyService.getCompanyEmployees(id);
        return ResponseEntity.ok(employees.stream().map(employeeMapper::toDto).toList());
    }

    @Operation(
            summary = "Get all locations of a company",
            description = """
                    Retrieves a list of all locations (stores, branches, sites) belonging to a specific company.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, READONLYMANAGER, EMPLOYEE
                    
                    **Use Case:** View all locations in a company for management or selection purposes.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Locations retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationResponseDto>> getCompanyLocations(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        List<Location> locations = companyService.getCompanyLocations(id);
        return ResponseEntity.ok(locations.stream().map(locationMapper::toDto).toList());
    }

    @Operation(
            summary = "Update company information",
            description = """
                    Updates an existing company's information. All fields in the request will be updated.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Note:** COMPANYADMIN can only update their own company.
                    
                    **Use Case:** Modify company details such as contact information, legal name, etc.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Company updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or duplicate data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponseDto> updateCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequestDto requestDto
    ) {

        Company company = companyService.updateCompany(id, requestDto);

        return ResponseEntity.ok(companyMapper.toDto(company));

    }

    @Operation(
            summary = "Activate a company",
            description = """
                    Activates a previously deactivated company, making it active in the system.
                    
                    **Access:** SUPERADMIN only
                    
                    **Use Case:** Reactivate a company that was temporarily deactivated.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Company activated successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only SUPERADMIN can activate companies"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<CompanyResponseDto> activateCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {

        Company company = companyService.activateCompany(id);

        return ResponseEntity.ok(companyMapper.toDto(company));

    }

    @Operation(
            summary = "Deactivate a company",
            description = """
                    Deactivates a company, making it inactive in the system. This is a soft operation
                    that doesn't delete the company but prevents it from being used.
                    
                    **Access:** SUPERADMIN only
                    
                    **Use Case:** Temporarily disable a company without deleting its data.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Company deactivated successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only SUPERADMIN can deactivate companies"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CompanyResponseDto> deactivateCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        Company company = companyService.deactivateCompany(id);
        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @Operation(
            summary = "Delete a company (soft delete)",
            description = """
                    Performs a soft delete on a company. The company is marked as deleted but data
                    is retained in the database for audit purposes.
                    
                    **Access:** SUPERADMIN only
                    
                    **Use Case:** Permanently remove a company from the system.
                    
                    **Note:** This operation cannot be undone. Consider deactivating instead if
                    you may need to restore the company later.
                    """,
            tags = {"Companies"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Company deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Only SUPERADMIN can delete companies"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Company not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long id
    ) {

        companyService.deleteCompany(id);

        return ResponseEntity.noContent().build();

    }

}
