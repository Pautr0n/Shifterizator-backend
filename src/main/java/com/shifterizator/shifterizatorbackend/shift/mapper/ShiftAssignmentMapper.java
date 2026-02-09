package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentResponseDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShiftAssignmentMapper {

    public ShiftAssignmentResponseDto toDto(ShiftAssignment assignment) {
        return toDto(assignment, null);
    }

    public ShiftAssignmentResponseDto toDto(ShiftAssignment assignment, List<String> warnings) {
        String employeeName = assignment.getEmployee() != null
                ? assignment.getEmployee().getName() + " " + assignment.getEmployee().getSurname()
                : null;
        List<String> safeWarnings = warnings != null ? warnings : List.of();
        return new ShiftAssignmentResponseDto(
                assignment.getId(),
                assignment.getShiftInstance() != null ? assignment.getShiftInstance().getId() : null,
                assignment.getEmployee() != null ? assignment.getEmployee().getId() : null,
                employeeName,
                assignment.getIsConfirmed(),
                assignment.getAssignedAt(),
                assignment.getAssignedBy(),
                assignment.getConfirmedAt(),
                safeWarnings
        );
    }
}
