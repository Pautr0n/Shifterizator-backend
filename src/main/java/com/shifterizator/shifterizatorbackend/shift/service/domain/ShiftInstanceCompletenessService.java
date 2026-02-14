package com.shifterizator.shifterizatorbackend.shift.service.domain;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftInstanceCompletenessService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;

    public void updateCompleteness(ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(
                shiftInstance.getId());

        boolean isComplete = shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .allMatch(stp -> {
                    long assignedCount = assignments.stream()
                            .filter(a -> a.getEmployee().getPosition().getId().equals(stp.getPosition().getId()))
                            .count();
                    return assignedCount >= stp.getRequiredCount();
                });

        shiftInstance.setIsComplete(isComplete);
    }
}
