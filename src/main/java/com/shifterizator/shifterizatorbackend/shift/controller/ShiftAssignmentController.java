package com.shifterizator.shifterizatorbackend.shift.controller;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentResponseDto;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftAssignmentMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shift-assignments")
@RequiredArgsConstructor
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftAssignmentMapper shiftAssignmentMapper;

    @PostMapping
    public ResponseEntity<ShiftAssignmentResponseDto> assign(@Valid @RequestBody ShiftAssignmentRequestDto dto) {
        var result = shiftAssignmentService.assign(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftAssignmentMapper.toDto(result.assignment(), result.warnings()));
    }

    @DeleteMapping("/shift-instance/{shiftInstanceId}/employee/{employeeId}")
    public ResponseEntity<Void> unassign(@PathVariable Long shiftInstanceId, @PathVariable Long employeeId) {
        shiftAssignmentService.unassign(shiftInstanceId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftAssignmentResponseDto> findById(@PathVariable Long id) {
        ShiftAssignment assignment = shiftAssignmentService.findById(id);
        return ResponseEntity.ok(shiftAssignmentMapper.toDto(assignment));
    }

    @GetMapping("/shift-instance/{shiftInstanceId}")
    public ResponseEntity<List<ShiftAssignmentResponseDto>> findByShiftInstance(@PathVariable Long shiftInstanceId) {
        List<ShiftAssignment> list = shiftAssignmentService.findByShiftInstance(shiftInstanceId);
        return ResponseEntity.ok(list.stream().map(shiftAssignmentMapper::toDto).toList());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftAssignmentResponseDto>> findByEmployee(@PathVariable Long employeeId) {
        List<ShiftAssignment> list = shiftAssignmentService.findByEmployee(employeeId);
        return ResponseEntity.ok(list.stream().map(shiftAssignmentMapper::toDto).toList());
    }
}
