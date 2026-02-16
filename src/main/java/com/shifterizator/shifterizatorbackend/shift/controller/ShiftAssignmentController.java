package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftAssignmentMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/shift-assignments")
@RequiredArgsConstructor
@Tag(
        name = "Shift assignments",
        description = "Endpoints for assigning and unassigning employees to shift instances. " +
                "Requires appropriate role (e.g. COMPANYADMIN, LOCATIONADMIN)."
)
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftAssignmentMapper shiftAssignmentMapper;

    @Operation(
            summary = "Assign employee to shift",
            description = "Assigns an employee to a shift instance. Response may include preference warnings (e.g. assigned on preferred day off).",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or conflict (e.g. already assigned)"),
            @ApiResponse(responseCode = "404", description = "Shift instance or employee not found")
    })
    @PostMapping
    public ResponseEntity<ShiftAssignmentResponseDto> assign(@Valid @RequestBody ShiftAssignmentRequestDto dto) {
        var result = shiftAssignmentService.assign(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftAssignmentMapper.toDto(result.assignment(), result.warnings()));
    }

    @Operation(
            summary = "Unassign employee from shift",
            description = "Removes an employee's assignment from a shift instance.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment removed successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @DeleteMapping("/shift-instance/{shiftInstanceId}/employee/{employeeId}")
    public ResponseEntity<Void> unassign(
            @Parameter(description = "Shift instance ID", required = true) @PathVariable Long shiftInstanceId,
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId) {
        shiftAssignmentService.unassign(shiftInstanceId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Confirm a shift assignment",
            description = "Marks a shift assignment as confirmed by the employee.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment confirmed successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ShiftAssignmentResponseDto> confirm(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id) {
        ShiftAssignment assignment = shiftAssignmentService.confirm(id);
        return ResponseEntity.ok(shiftAssignmentMapper.toDto(assignment));
    }

    @Operation(
            summary = "Get assignment by ID",
            description = "Retrieves a shift assignment by its ID.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Assignment not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ShiftAssignmentResponseDto> findById(
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long id) {
        ShiftAssignment assignment = shiftAssignmentService.findById(id);
        return ResponseEntity.ok(shiftAssignmentMapper.toDto(assignment));
    }

    @Operation(
            summary = "Get assignments for a shift instance",
            description = "Returns all employee assignments for a given shift instance.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of assignments")
    })
    @GetMapping("/shift-instance/{shiftInstanceId}")
    public ResponseEntity<List<ShiftAssignmentResponseDto>> findByShiftInstance(
            @Parameter(description = "Shift instance ID", required = true) @PathVariable Long shiftInstanceId) {
        List<ShiftAssignment> list = shiftAssignmentService.findByShiftInstance(shiftInstanceId);
        return ResponseEntity.ok(list.stream().map(shiftAssignmentMapper::toDto).toList());
    }

    @Operation(
            summary = "Get assignments for an employee",
            description = "Returns all shift assignments for a given employee.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of assignments")
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentResponseDto>> findByEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId) {
        List<ShiftAssignment> list = shiftAssignmentService.findByEmployee(employeeId);
        return ResponseEntity.ok(list.stream().map(shiftAssignmentMapper::toDto).toList());
    }
}
