package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.GenerateMonthRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ScheduleDayRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftGenerationService;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftInstanceService;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftSchedulerService;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceRequirementStatusService;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shift-instances")
@RequiredArgsConstructor
@Tag(
        name = "Shift instances",
        description = "Endpoints for managing concrete shift occurrences (dates/times) and generating/scheduling shifts. " +
                "Requires appropriate role (e.g. COMPANYADMIN, LOCATIONADMIN)."
)
public class ShiftInstanceController {

    private final ShiftInstanceService shiftInstanceService;
    private final ShiftInstanceMapper shiftInstanceMapper;
    private final ShiftGenerationService shiftGenerationService;
    private final ShiftSchedulerService shiftSchedulerService;
    private final ShiftInstanceRequirementStatusService requirementStatusService;

    @Operation(
            summary = "Generate shift instances for a month",
            description = "Generates shift instances for a location for the given year-month based on shift templates.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shift instances generated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PostMapping("/generate-month")
    public ResponseEntity<List<ShiftInstanceResponseDto>> generateMonth(@Valid @RequestBody GenerateMonthRequestDto dto) {
        YearMonth yearMonth = YearMonth.of(dto.year(), dto.month());
        List<ShiftInstance> instances = shiftGenerationService.generateMonth(dto.locationId(), yearMonth);
        List<ShiftInstanceResponseDto> body = instances.stream()
                .map(i -> toDtoWithStatus(i))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(
            summary = "Schedule a single day",
            description = "Triggers auto-assignment of employees to shifts for a location on a given date.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Scheduling accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PostMapping("/schedule-day")
    public ResponseEntity<Void> scheduleDay(@Valid @RequestBody ScheduleDayRequestDto dto) {
        shiftSchedulerService.scheduleDay(dto.locationId(), dto.date());
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Schedule a full month",
            description = "Triggers auto-assignment of employees to shifts for a location for the given month.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Scheduling accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    @PostMapping("/schedule-month")
    public ResponseEntity<Void> scheduleMonth(@Valid @RequestBody GenerateMonthRequestDto dto) {
        shiftSchedulerService.scheduleMonth(dto.locationId(), YearMonth.of(dto.year(), dto.month()));
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Create a shift instance",
            description = "Creates a single shift instance (one shift slot on a date at a location).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shift instance created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Template or location not found")
    })
    @PostMapping
    public ResponseEntity<ShiftInstanceResponseDto> create(@Valid @RequestBody ShiftInstanceRequestDto dto) {
        ShiftInstance instance = shiftInstanceService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDtoWithStatus(instance));
    }

    @Operation(
            summary = "Update a shift instance",
            description = "Updates an existing shift instance.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shift instance updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Shift instance not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ShiftInstanceResponseDto> update(
            @Parameter(description = "Shift instance ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ShiftInstanceRequestDto dto) {
        ShiftInstance instance = shiftInstanceService.update(id, dto);
        return ResponseEntity.ok(toDtoWithStatus(instance));
    }

    @Operation(
            summary = "Delete a shift instance",
            description = "Deletes a shift instance. Use hardDelete=true to remove permanently when assignments exist.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shift instance deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Shift instance not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Shift instance ID", required = true) @PathVariable Long id,
            @Parameter(description = "If true, permanently delete even when assignments exist") @RequestParam(defaultValue = "false") boolean hardDelete) {
        shiftInstanceService.delete(id, hardDelete);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get shift instance by ID",
            description = "Retrieves a shift instance by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shift instance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Shift instance not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ShiftInstanceResponseDto> findById(
            @Parameter(description = "Shift instance ID", required = true) @PathVariable Long id) {
        ShiftInstance instance = shiftInstanceService.findById(id);
        return ResponseEntity.ok(toDtoWithStatus(instance));
    }

    @Operation(
            summary = "Search shift instances",
            description = "Paginated search of shift instances by location and/or date range.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of shift instances")
    })
    @GetMapping
    public ResponseEntity<Page<ShiftInstanceResponseDto>> search(
            @Parameter(description = "Filter by location ID") @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start date (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable
    ) {
        Page<ShiftInstance> page = shiftInstanceService.search(locationId, startDate, endDate, pageable);
        return ResponseEntity.ok(page.map(this::toDtoWithStatus));
    }

    @Operation(
            summary = "Get shift instances by location and date",
            description = "Returns all shift instances for a location on a specific date.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of shift instances")
    })
    @GetMapping("/by-location/{locationId}/date/{date}")
    public ResponseEntity<List<ShiftInstanceResponseDto>> findByLocationAndDate(
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Date (ISO)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ShiftInstance> list = shiftInstanceService.findByLocationAndDate(locationId, date);
        return ResponseEntity.ok(list.stream().map(this::toDtoWithStatus).toList());
    }

    @Operation(
            summary = "Get shift instances by location and date range",
            description = "Returns all shift instances for a location between start and end date.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of shift instances")
    })
    @GetMapping("/by-location/{locationId}/range")
    public ResponseEntity<List<ShiftInstanceResponseDto>> findByLocationAndDateRange(
            @Parameter(description = "Location ID", required = true) @PathVariable Long locationId,
            @Parameter(description = "Start date (ISO)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<ShiftInstance> list = shiftInstanceService.findByLocationAndDateRange(locationId, startDate, endDate);
        return ResponseEntity.ok(list.stream().map(this::toDtoWithStatus).toList());
    }

    private ShiftInstanceResponseDto toDtoWithStatus(ShiftInstance instance) {
        int assignedCount = shiftInstanceService.getAssignedCount(instance.getId());
        var positionStatus = requirementStatusService.getPositionRequirementStatus(instance);
        var languageStatus = requirementStatusService.getLanguageRequirementStatus(instance);
        return shiftInstanceMapper.toDto(instance, assignedCount, positionStatus, languageStatus);
    }
}
