package com.shifterizator.shifterizatorbackend.shift.service.domain;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Domain service responsible for calculating and updating shift instance completeness.
 * A shift instance is considered complete when all position requirements are met.
 */
@Service
@RequiredArgsConstructor
public class ShiftInstanceCompletenessService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;

    /**
     * Updates the completeness status of a shift instance based on current assignments.
     * A shift instance is complete when all required positions have at least the required number of assigned employees.
     *
     * @param shiftInstance the shift instance to update
     */
    public void updateCompleteness(ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(
                shiftInstance.getId());

        // Check if all position requirements are met
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
