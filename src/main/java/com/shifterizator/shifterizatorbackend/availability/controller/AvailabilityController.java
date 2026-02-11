package com.shifterizator.shifterizatorbackend.availability.controller;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.mapper.AvailabilityMapper;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(
        name = "Employee availability",
        description = "Endpoints for managing employee availability (available, vacation, sick leave, etc.). " +
                "Blocking types (e.g. vacation, sick leave) prevent scheduling. Requires appropriate role."
)
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    @Operation(
            summary = "Create availability",
            description = "Creates an availability entry for an employee (e.g. available, vacation, sick leave).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Availability created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> create(@Valid @RequestBody AvailabilityRequestDto dto) {
        EmployeeAvailability availability = availabilityService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(availabilityMapper.toDto(availability));
    }

    @Operation(
            summary = "Update availability",
            description = "Updates an existing availability entry.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> update(
            @Parameter(description = "Availability ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequestDto dto) {
        EmployeeAvailability availability = availabilityService.update(id, dto);
        return ResponseEntity.ok(availabilityMapper.toDto(availability));
    }

    @Operation(
            summary = "Delete availability",
            description = "Deletes an availability entry. Use hardDelete=true to remove permanently.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Availability deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Availability ID", required = true) @PathVariable Long id,
            @Parameter(description = "If true, permanently delete") @RequestParam(defaultValue = "false") boolean hardDelete) {
        availabilityService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get availability by ID",
            description = "Retrieves an availability entry by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> findById(
            @Parameter(description = "Availability ID", required = true) @PathVariable Long id) {
        EmployeeAvailability availability = availabilityService.findById(id);
        return ResponseEntity.ok(availabilityMapper.toDto(availability));
    }

    @Operation(
            summary = "Search availability",
            description = "Paginated search by employee, type, location, and/or date range.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of availability entries")
    })
    @GetMapping
    public ResponseEntity<Page<AvailabilityResponseDto>> search(
            @Parameter(description = "Filter by employee ID") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Filter by type (AVAILABLE, VACATION, SICK_LEAVE, etc.)") @RequestParam(required = false) AvailabilityType type,
            @Parameter(description = "Filter by location ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start date (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                availabilityService.search(employeeId, type, locationId, start, end, pageable)
                        .map(availabilityMapper::toDto)
        );
    }

    @Operation(
            summary = "Get availability by employee",
            description = "Returns all availability entries for an employee.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of availability entries")
    })
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<List<AvailabilityResponseDto>> findByEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId) {
        List<EmployeeAvailability> list = availabilityService.findByEmployee(employeeId);
        return ResponseEntity.ok(list.stream().map(availabilityMapper::toDto).toList());
    }

    @Operation(
            summary = "Get availability by date range",
            description = "Returns all availability entries overlapping the given date range.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of availability entries")
    })
    @GetMapping("/range")
    public ResponseEntity<List<AvailabilityResponseDto>> findByRange(
            @Parameter(description = "Start date (ISO)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (ISO)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<EmployeeAvailability> list = availabilityService.findByRange(start, end);
        return ResponseEntity.ok(list.stream().map(availabilityMapper::toDto).toList());
    }
}
