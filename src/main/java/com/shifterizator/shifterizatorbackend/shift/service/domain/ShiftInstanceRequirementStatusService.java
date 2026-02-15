package com.shifterizator.shifterizatorbackend.shift.service.domain;

import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeLanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.LanguageRequirementStatusDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementStatusDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplateLanguageRequirement;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftInstanceRequirementStatusService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeLanguageRepository employeeLanguageRepository;

    public List<PositionRequirementStatusDto> getPositionRequirementStatus(ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(shiftInstance.getId());
        var template = shiftInstance.getShiftTemplate();
        if (template == null || template.getRequiredPositions() == null) {
            return List.of();
        }
        List<PositionRequirementStatusDto> result = new ArrayList<>();
        for (ShiftTemplatePosition stp : template.getRequiredPositions()) {
            long assignedCount = assignments.stream()
                    .filter(a -> a.getEmployee() != null && a.getEmployee().getPosition() != null
                            && a.getEmployee().getPosition().getId().equals(stp.getPosition().getId()))
                    .count();
            result.add(new PositionRequirementStatusDto(
                    stp.getPosition().getId(),
                    stp.getPosition().getName(),
                    stp.getRequiredCount(),
                    stp.getIdealCount(),
                    (int) assignedCount
            ));
        }
        return result;
    }

    public List<LanguageRequirementStatusDto> getLanguageRequirementStatus(ShiftInstance shiftInstance) {
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(shiftInstance.getId());
        var template = shiftInstance.getShiftTemplate();
        if (template == null || template.getRequiredLanguageRequirements() == null || template.getRequiredLanguageRequirements().isEmpty()) {
            return List.of();
        }
        List<Long> assignedEmployeeIds = assignments.stream()
                .map(a -> a.getEmployee().getId())
                .toList();
        if (assignedEmployeeIds.isEmpty()) {
            return template.getRequiredLanguageRequirements().stream()
                    .map(req -> new LanguageRequirementStatusDto(
                            req.getLanguage().getId(),
                            req.getLanguage().getName(),
                            req.getRequiredCount(),
                            0
                    ))
                    .toList();
        }
        var employeeLanguages = employeeLanguageRepository.findByEmployee_IdIn(assignedEmployeeIds);
        List<LanguageRequirementStatusDto> result = new ArrayList<>();
        for (ShiftTemplateLanguageRequirement req : template.getRequiredLanguageRequirements()) {
            Long languageId = req.getLanguage().getId();
            Set<Long> distinctEmployeesWithLanguage = employeeLanguages.stream()
                    .filter(el -> el.getLanguage().getId().equals(languageId))
                    .map(el -> el.getEmployee().getId())
                    .collect(Collectors.toSet());
            result.add(new LanguageRequirementStatusDto(
                    req.getLanguage().getId(),
                    req.getLanguage().getName(),
                    req.getRequiredCount(),
                    distinctEmployeesWithLanguage.size()
            ));
        }
        return result;
    }
}
