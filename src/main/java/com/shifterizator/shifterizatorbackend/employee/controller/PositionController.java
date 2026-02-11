package com.shifterizator.shifterizatorbackend.employee.controller;

import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.mapper.PositionMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
@Tag(
        name = "Positions",
        description = "Endpoints for managing job positions/titles within a company. " +
                "Creation and management restricted to SUPERADMIN and COMPANYADMIN."
)
public class PositionController {

    private final PositionService positionService;
    private final PositionMapper positionMapper;

    @Operation(
            summary = "Create a position",
            description = "Creates a new job position (e.g. Cashier, Manager) for a company.",
            tags = {"Positions"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Position created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<PositionDto> create(
            @Parameter(description = "Position name", example = "Cashier", required = true) @RequestParam String name,
            @Parameter(description = "Company ID", example = "1", required = true) @RequestParam Long companyId) {
        Position position = positionService.create(name, companyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(positionMapper.toDto(position));
    }

    @Operation(
            summary = "Update a position",
            description = "Updates an existing position's name.",
            tags = {"Positions"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position updated successfully"),
            @ApiResponse(responseCode = "404", description = "Position not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PositionDto> update(
            @Parameter(description = "Position ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "New position name", example = "Senior Cashier", required = true) @RequestParam String name) {
        Position position = positionService.update(id, name);
        return ResponseEntity.ok(positionMapper.toDto(position));
    }

    @Operation(
            summary = "Delete a position",
            description = "Deletes a position from the system.",
            tags = {"Positions"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Position deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Position not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Position ID", example = "1", required = true) @PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get position by ID",
            description = "Retrieves a position by its ID.",
            tags = {"Positions"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Position not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PositionDto> findById(
            @Parameter(description = "Position ID", example = "1", required = true) @PathVariable Long id) {
        Position position = positionService.findById(id);
        return ResponseEntity.ok(positionMapper.toDto(position));
    }

    @Operation(
            summary = "Get all positions for a company",
            description = "Retrieves all job positions belonging to a company.",
            tags = {"Positions"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions retrieved successfully")
    })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<PositionDto>> findByCompany(
            @Parameter(description = "Company ID", example = "1", required = true) @PathVariable Long companyId) {
        List<Position> positions = positionService.findByCompany(companyId);
        return ResponseEntity.ok(positions.stream().map(positionMapper::toDto).toList());
    }
}