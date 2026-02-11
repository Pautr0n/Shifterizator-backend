package com.shifterizator.shifterizatorbackend.openinghours.controller;

import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
import com.shifterizator.shifterizatorbackend.openinghours.mapper.SpecialOpeningHoursMapper;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.service.SpecialOpeningHoursService;
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

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/opening-hours")
@RequiredArgsConstructor
@Tag(
        name = "Special opening hours",
        description = "Endpoints for managing special opening hours (e.g. holiday hours) per location. " +
                "Requires appropriate role (e.g. COMPANYADMIN, LOCATIONADMIN)."
)
public class SpecialOpeningHoursController {

    private final SpecialOpeningHoursService openingHoursService;
    private final SpecialOpeningHoursMapper openingHoursMapper;

    @Operation(
            summary = "Create special opening hours",
            description = "Creates special opening hours for a location on a date (e.g. holiday schedule).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Special opening hours created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PostMapping
    public ResponseEntity<SpecialOpeningHoursResponseDto> create(@Valid @RequestBody SpecialOpeningHoursRequestDto dto) {
        SpecialOpeningHours openingHours = openingHoursService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(openingHoursMapper.toDto(openingHours));
    }

    @Operation(
            summary = "Update special opening hours",
            description = "Updates existing special opening hours.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special opening hours updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Special opening hours not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SpecialOpeningHoursResponseDto> update(
            @Parameter(description = "Special opening hours ID", required = true) @PathVariable Long id,
            @Valid @RequestBody SpecialOpeningHoursRequestDto dto) {
        SpecialOpeningHours openingHours = openingHoursService.update(id, dto);
        return ResponseEntity.ok(openingHoursMapper.toDto(openingHours));
    }

    @Operation(
            summary = "Delete special opening hours",
            description = "Deletes special opening hours. Use hardDelete=true to remove permanently.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Special opening hours deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Special opening hours not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Special opening hours ID", required = true) @PathVariable Long id,
            @Parameter(description = "If true, permanently delete") @RequestParam(defaultValue = "false") boolean hardDelete) {
        openingHoursService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get special opening hours by ID",
            description = "Retrieves special opening hours by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Special opening hours retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Special opening hours not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SpecialOpeningHoursResponseDto> findById(
            @Parameter(description = "Special opening hours ID", required = true) @PathVariable Long id) {
        SpecialOpeningHours openingHours = openingHoursService.findById(id);
        return ResponseEntity.ok(openingHoursMapper.toDto(openingHours));
    }

    @Operation(
            summary = "Search special opening hours",
            description = "Paginated search by location and/or company.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of special opening hours")
    })
    @GetMapping
    public ResponseEntity<Page<SpecialOpeningHoursResponseDto>> search(
            @Parameter(description = "Filter by location ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                openingHoursService.search(locationId, companyId, pageable)
                        .map(openingHoursMapper::toDto)
        );
    }

    @Operation(
            summary = "Get special opening hours by location",
            description = "Returns all special opening hours for a location.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of special opening hours")
    })
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<SpecialOpeningHoursResponseDto>> findByLocation(
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        List<SpecialOpeningHours> list = openingHoursService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(openingHoursMapper::toDto).toList());
    }

    @Operation(
            summary = "Get special opening hours by location and month",
            description = "Returns special opening hours for a location in a given month (yyyy-MM).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of special opening hours")
    })
    @GetMapping("/month")
    public ResponseEntity<List<SpecialOpeningHoursResponseDto>> findByLocationAndMonth(
            @Parameter(description = "Location ID", required = true) @RequestParam Long locationId,
            @Parameter(description = "Month (yyyy-MM)", example = "2025-02", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        List<SpecialOpeningHours> list = openingHoursService.findByLocationAndMonth(locationId, month);
        return ResponseEntity.ok(list.stream().map(openingHoursMapper::toDto).toList());
    }
}
