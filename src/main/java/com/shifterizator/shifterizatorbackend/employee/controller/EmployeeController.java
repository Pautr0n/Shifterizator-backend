package com.shifterizator.shifterizatorbackend.employee.controller;


import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.employee.access.EmployeeAccessPolicy;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
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

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(
        name = "Employees",
        description = "Endpoints for managing employees, their preferences, and assignments. " +
                "Employees can update their own preferences. " +
                "Managers can create, update, and delete employees."
)
public class EmployeeController {

    private final EmployeeMapper employeeMapper;
    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;
    private final EmployeeAccessPolicy employeeAccessPolicy;

    @Operation(
            summary = "Create a new employee",
            description = """
                    Creates a new employee and assigns them to companies, locations, and a position.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER
                    
                    **Use Case:** Add a new employee to the system when hiring or onboarding.
                    
                    **Required Fields:**
                    - Name and surname
                    - Position ID
                    - At least one company ID
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = EmployeeResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or invalid references"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            )
    })
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto dto) {
        employeeAccessPolicy.ensureCompanyScopeForCreate(dto, currentUserService.getCurrentUser());
        Employee employee = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeMapper.toDto(employee));
    }

    @Operation(
            summary = "Update employee information",
            description = """
                    Updates an existing employee's information. Employees can update their own preferences
                    via the preferences endpoint, but this endpoint allows managers to update all employee data.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, EMPLOYEE
                    
                    **Note:** EMPLOYEE can only update their own information.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto dto
    ) {
        employeeAccessPolicy.ensureCanAccessEmployee(employeeService.findById(id), currentUserService.getCurrentUser());
        Employee employee = employeeService.update(id, dto);
        return ResponseEntity.ok((employeeMapper.toDto(employee)));
    }

    @Operation(
            summary = "Delete an employee",
            description = """
                    Deletes an employee from the system. By default, performs a soft delete (marks as deleted
                    but retains data). Use hardDelete=true for permanent deletion.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER
                    
                    **Use Case:** Remove an employee when they leave the company.
                    
                    **Note:** Hard delete permanently removes the employee and cannot be undone.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Employee deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "If true, performs hard delete (permanent). Default: false", example = "false")
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        employeeAccessPolicy.ensureCanAccessEmployee(employeeService.findById(id), currentUserService.getCurrentUser());
        employeeService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get employee by ID",
            description = """
                    Retrieves detailed information about a specific employee.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, READONLYMANAGER, EMPLOYEE
                    
                    **Use Case:** View employee details, edit employee information.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> findById(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        Employee employee = employeeService.findById(id);
        employeeAccessPolicy.ensureCanAccessEmployee(employee, currentUserService.getCurrentUser());
        return ResponseEntity.ok(employeeMapper.toDto(employee));
    }

    @Operation(
            summary = "Get employee preferences",
            description = """
                    Retrieves an employee's preferences including preferred day off and preferred shift templates.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, READONLYMANAGER, EMPLOYEE
                    
                    **Use Case:** View employee preferences for scheduling purposes.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}/preferences")
    public ResponseEntity<EmployeePreferencesResponseDto> getPreferences(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        employeeAccessPolicy.ensureCanAccessEmployee(employeeService.findById(id), currentUserService.getCurrentUser());
        return ResponseEntity.ok(employeeService.getPreferences(id));
    }

    @Operation(
            summary = "Update employee preferences",
            description = """
                    Updates an employee's preferences (preferred day off and preferred shift templates).
                    Employees can update their own preferences.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, EMPLOYEE
                    
                    **Use Case:** Allow employees to set their scheduling preferences, or managers to adjust them.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Preferences updated successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            )
    })
    @PutMapping("/{id}/preferences")
    public ResponseEntity<EmployeePreferencesResponseDto> updatePreferences(
            @Parameter(description = "Employee ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody EmployeePreferencesRequestDto dto
    ) {
        employeeAccessPolicy.ensureCanAccessEmployee(employeeService.findById(id), currentUserService.getCurrentUser());
        return ResponseEntity.ok(employeeService.updatePreferences(id, dto));
    }

    @Operation(
            summary = "Search employees with filters and pagination",
            description = """
                    Retrieves a paginated list of employees with optional filtering capabilities.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER, READONLYMANAGER, EMPLOYEE
                    
                    **Filters:**
                    - `companyId`: Filter by company
                    - `locationId`: Filter by location
                    - `name`: Filter by employee name (partial match)
                    - `position`: Filter by position name
                    
                    **Pagination:**
                    - `page`: Page number (0-indexed, default: 0)
                    - `size`: Page size (default: 20)
                    - `sort`: Sort criteria (e.g., `name,asc`)
                    
                    **Use Case:** Browse and search employees in the system.
                    """,
            tags = {"Employees"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDto>> search(
            @Parameter(description = "Filter by company ID", example = "1")
            @RequestParam(required = false) Long companyId,
            
            @Parameter(description = "Filter by location ID", example = "2")
            @RequestParam(required = false) Long locationId,
            
            @Parameter(description = "Filter by employee name (partial match)", example = "John")
            @RequestParam(required = false) String name,
            
            @Parameter(description = "Filter by position name", example = "Cashier")
            @RequestParam(required = false) String position,
            
            @Parameter(description = "Pagination parameters (page, size, sort)")
            Pageable pageable
    ) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.COMPANYADMIN && currentUser.getCompany() != null) {
            companyId = currentUser.getCompany().getId();
        }
        Page<EmployeeResponseDto> result = employeeService.search(companyId,
                        locationId,
                        name,
                        position,
                        pageable)
                .map(employeeMapper::toDto);
        return ResponseEntity.ok(result);
    }
}
