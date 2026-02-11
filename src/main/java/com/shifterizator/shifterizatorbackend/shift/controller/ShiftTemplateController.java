package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
@RequestMapping("/api/shift-templates")
@RequiredArgsConstructor
@Tag(
        name = "Shift Templates",
        description = "Endpoints for managing shift templates. Templates define the structure of shifts " +
                "that can be automatically generated. Creation/deletion restricted to SUPERADMIN and COMPANYADMIN. " +
                "SHIFTMANAGER can update templates."
)
public class ShiftTemplateController {

    private final ShiftTemplateService shiftTemplateService;
    private final ShiftTemplateMapper shiftTemplateMapper;

    @Operation(
            summary = "Create a shift template",
            description = """
                    Creates a new shift template that defines the structure of shifts (time, location, positions, etc.).
                    Templates are used to automatically generate shift instances.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Use Case:** Define recurring shift patterns (e.g., "Morning Cashier Shift 9am-5pm").
                    """,
            tags = {"Shift Templates"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Shift template created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions"
            )
    })
    @PostMapping
    public ResponseEntity<ShiftTemplateResponseDto> create(@Valid @RequestBody ShiftTemplateRequestDto dto) {
        ShiftTemplate template = shiftTemplateService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftTemplateMapper.toDto(template));
    }

    @Operation(
            summary = "Update a shift template",
            description = """
                    Updates an existing shift template.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN, SHIFTMANAGER
                    
                    **Use Case:** Modify shift template details (times, positions, etc.).
                    """,
            tags = {"Shift Templates"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}")
    public ResponseEntity<ShiftTemplateResponseDto> update(
            @Parameter(description = "Shift template ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ShiftTemplateRequestDto dto
    ) {
        ShiftTemplate template = shiftTemplateService.update(id, dto);
        return ResponseEntity.ok(shiftTemplateMapper.toDto(template));
    }

    @Operation(
            summary = "Delete a shift template",
            description = """
                    Deletes a shift template. By default, performs soft delete.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Use Case:** Remove shift templates that are no longer needed.
                    """,
            tags = {"Shift Templates"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Shift template ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "If true, performs hard delete", example = "false")
            @RequestParam(defaultValue = "false") boolean hardDelete
    ) {
        shiftTemplateService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get shift template by ID",
            description = """
                    Retrieves detailed information about a specific shift template.
                    
                    **Access:** All authenticated users
                    """,
            tags = {"Shift Templates"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}")
    public ResponseEntity<ShiftTemplateResponseDto> findById(
            @Parameter(description = "Shift template ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        ShiftTemplate template = shiftTemplateService.findById(id);
        return ResponseEntity.ok(shiftTemplateMapper.toDto(template));
    }

    @Operation(
            summary = "Search shift templates with filters and pagination",
            description = """
                    Retrieves a paginated list of shift templates with optional filtering.
                    
                    **Access:** All authenticated users
                    
                    **Filters:**
                    - `locationId`: Filter by location
                    - `positionId`: Filter by position
                    
                    **Use Case:** Browse available shift templates for a location or position.
                    """,
            tags = {"Shift Templates"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<Page<ShiftTemplateResponseDto>> search(
            @Parameter(description = "Filter by location ID", example = "1")
            @RequestParam(required = false) Long locationId,
            
            @Parameter(description = "Filter by position ID", example = "2")
            @RequestParam(required = false) Long positionId,
            
            @Parameter(description = "Pagination parameters")
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                shiftTemplateService.search(locationId, positionId, pageable)
                        .map(shiftTemplateMapper::toDto)
        );
    }

    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<ShiftTemplateResponseDto>> findByLocation(@PathVariable Long locationId) {
        List<ShiftTemplate> list = shiftTemplateService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(shiftTemplateMapper::toDto).toList());
    }
}
