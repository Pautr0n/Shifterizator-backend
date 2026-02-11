package com.shifterizator.shifterizatorbackend.blackoutdays.controller;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.mapper.BlackoutDayMapper;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.service.BlackoutDayService;
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
@RequestMapping("/api/blackout-days")
@RequiredArgsConstructor
@Tag(
        name = "Blackout days",
        description = "Endpoints for managing blackout days (dates when a location is closed or has no shifts). " +
                "Requires appropriate role (e.g. COMPANYADMIN, LOCATIONADMIN)."
)
public class BlackoutDayController {

    private final BlackoutDayService blackoutDayService;
    private final BlackoutDayMapper blackoutDayMapper;

    @Operation(summary = "Create blackout day", description = "Creates a blackout day for a location (e.g. holiday, closure).", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Blackout day created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PostMapping
    public ResponseEntity<BlackoutDayResponseDto> create(@Valid @RequestBody BlackoutDayRequestDto dto) {
        BlackoutDay blackoutDay = blackoutDayService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(blackoutDayMapper.toDto(blackoutDay));
    }

    @Operation(summary = "Update blackout day", description = "Updates an existing blackout day.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blackout day updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Blackout day not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BlackoutDayResponseDto> update(
            @Parameter(description = "Blackout day ID", required = true) @PathVariable Long id,
            @Valid @RequestBody BlackoutDayRequestDto dto) {
        BlackoutDay blackoutDay = blackoutDayService.update(id, dto);
        return ResponseEntity.ok(blackoutDayMapper.toDto(blackoutDay));
    }

    @Operation(summary = "Delete blackout day", description = "Deletes a blackout day. Use hardDelete=true to remove permanently.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Blackout day deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blackout day not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Blackout day ID", required = true) @PathVariable Long id,
            @Parameter(description = "If true, permanently delete") @RequestParam(defaultValue = "false") boolean hardDelete) {
        blackoutDayService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get blackout day by ID", description = "Retrieves a blackout day by its ID.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Blackout day retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Blackout day not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BlackoutDayResponseDto> findById(
            @Parameter(description = "Blackout day ID", required = true) @PathVariable Long id) {
        BlackoutDay blackoutDay = blackoutDayService.findById(id);
        return ResponseEntity.ok(blackoutDayMapper.toDto(blackoutDay));
    }

    @Operation(summary = "Search blackout days", description = "Paginated search by location and/or company.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Page of blackout days") })
    @GetMapping
    public ResponseEntity<Page<BlackoutDayResponseDto>> search(
            @Parameter(description = "Filter by location ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                blackoutDayService.search(locationId, companyId, pageable)
                        .map(blackoutDayMapper::toDto)
        );
    }

    @Operation(summary = "Get blackout days by location", description = "Returns all blackout days for a location.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "List of blackout days") })
    @GetMapping("/by-location/{locationId}")
    public ResponseEntity<List<BlackoutDayResponseDto>> findByLocation(
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId) {
        List<BlackoutDay> list = blackoutDayService.findByLocation(locationId);
        return ResponseEntity.ok(list.stream().map(blackoutDayMapper::toDto).toList());
    }

    @Operation(summary = "Get blackout days by location and month", description = "Returns blackout days for a location in a given month (yyyy-MM).", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "List of blackout days") })
    @GetMapping("/month")
    public ResponseEntity<List<BlackoutDayResponseDto>> findByLocationAndMonth(
            @Parameter(description = "Location ID", required = true) @RequestParam Long locationId,
            @Parameter(description = "Month (yyyy-MM)", example = "2025-02", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        List<BlackoutDay> list = blackoutDayService.findByLocationAndMonth(locationId, month);
        return ResponseEntity.ok(list.stream().map(blackoutDayMapper::toDto).toList());
    }
}
