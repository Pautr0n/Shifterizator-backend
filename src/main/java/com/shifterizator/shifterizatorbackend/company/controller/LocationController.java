package com.shifterizator.shifterizatorbackend.company.controller;

import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(
        name = "Locations",
        description = "Endpoints for managing company locations (stores, branches, sites). " +
                "Creation and management restricted to SUPERADMIN and COMPANYADMIN. " +
                "All authenticated users can view locations."
)
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @Operation(
            summary = "Create a new location",
            description = """
                    Creates a new location (store, branch, or site) for a company.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    
                    **Use Case:** Add a new physical location when a company opens a new branch.
                    """,
            tags = {"Locations"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Location created successfully"
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
    public ResponseEntity<LocationResponseDto> create(@Valid @RequestBody LocationRequestDto dto) {
        Location location = locationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationMapper.toDto(location));
    }

    @Operation(
            summary = "Update location information",
            description = """
                    Updates an existing location's information.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    """,
            tags = {"Locations"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDto> update(
            @Parameter(description = "Location ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody LocationRequestDto dto
    ) {
        Location location = locationService.update(id, dto);
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @Operation(
            summary = "Delete a location",
            description = """
                    Deletes a location from the system.
                    
                    **Access:** SUPERADMIN, COMPANYADMIN
                    """,
            tags = {"Locations"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Location ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get location by ID",
            description = """
                    Retrieves detailed information about a specific location.
                    
                    **Access:** All authenticated users
                    """,
            tags = {"Locations"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDto> findById(
            @Parameter(description = "Location ID", example = "1", required = true)
            @PathVariable Long id
    ) {
        Location location = locationService.findById(id);
        return ResponseEntity.ok(locationMapper.toDto(location));
    }

    @Operation(
            summary = "Get all locations for a company",
            description = """
                    Retrieves a list of all locations belonging to a specific company.
                    
                    **Access:** All authenticated users
                    
                    **Use Case:** View all locations when selecting a location for shifts or employees.
                    """,
            tags = {"Locations"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<LocationResponseDto>> findByCompany(
            @Parameter(description = "Company ID", example = "1", required = true)
            @PathVariable Long companyId
    ) {
        List<Location> locations = locationService.findByCompany(companyId);
        return ResponseEntity.ok(locations.stream().map(locationMapper::toDto).toList());
    }
}