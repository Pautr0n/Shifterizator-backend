package com.shifterizator.shifterizatorbackend.shift.service.domain;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.LanguageRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplateLanguageRequirement;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftTemplateDomainService {

    private final LocationRepository locationRepository;
    private final PositionRepository positionRepository;
    private final LanguageRepository languageRepository;

    public Location resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
    }

    public Set<Language> resolveLanguages(Set<Long> languageIds) {
        if (languageIds == null || languageIds.isEmpty()) {
            return new HashSet<>();
        }

        return languageIds.stream()
                .map(id -> languageRepository.findById(id)
                        .orElseThrow(() -> new LanguageNotFoundException("Language not found")))
                .collect(Collectors.toSet());
    }

    public void buildLanguageRequirements(ShiftTemplate template, List<LanguageRequirementDto> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            template.getRequiredLanguageRequirements().clear();
            return;
        }
        Set<ShiftTemplateLanguageRequirement> set = new HashSet<>();
        for (LanguageRequirementDto req : requirements) {
            if (req.requiredCount() == null || req.requiredCount() <= 0) {
                continue;
            }
            Language language = languageRepository.findById(req.languageId())
                    .orElseThrow(() -> new LanguageNotFoundException("Language not found: " + req.languageId()));
            set.add(ShiftTemplateLanguageRequirement.builder()
                    .shiftTemplate(template)
                    .language(language)
                    .requiredCount(req.requiredCount())
                    .build());
        }
        template.getRequiredLanguageRequirements().clear();
        template.getRequiredLanguageRequirements().addAll(set);
    }

    public void buildPositionRequirements(ShiftTemplate template, List<PositionRequirementDto> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            if (template.getRequiredPositions() != null) {
                template.getRequiredPositions().clear();
            }
            return;
        }

        Set<ShiftTemplatePosition> set = template.getRequiredPositions();
        if (set == null) {
            set = new HashSet<>();
            template.setRequiredPositions(set);
        }

        Map<Long, PositionRequirementDto> byPositionId = requirements.stream()
                .collect(Collectors.toMap(PositionRequirementDto::positionId, r -> r, (a, b) -> a));

        set.removeIf(stp -> {
            Long posId = stp.getPosition() != null ? stp.getPosition().getId() : null;
            if (posId == null) {
                return true;
            }
            PositionRequirementDto dto = byPositionId.get(posId);
            if (dto == null) {
                return true;
            }
            validateIdealCount(dto.requiredCount(), dto.idealCount());
            stp.setRequiredCount(dto.requiredCount());
            stp.setIdealCount(dto.idealCount());
            return false;
        });

        Set<Long> existingPositionIds = set.stream()
                .map(stp -> stp.getPosition() != null ? stp.getPosition().getId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (PositionRequirementDto req : requirements) {
            if (existingPositionIds.contains(req.positionId())) {
                continue;
            }
            validateIdealCount(req.requiredCount(), req.idealCount());
            Position position = positionRepository.findById(req.positionId())
                    .orElseThrow(() -> new PositionNotFoundException("Position not found: " + req.positionId()));
            ShiftTemplatePosition templatePosition = ShiftTemplatePosition.builder()
                    .shiftTemplate(template)
                    .position(position)
                    .requiredCount(req.requiredCount())
                    .idealCount(req.idealCount())
                    .build();
            set.add(templatePosition);
            existingPositionIds.add(req.positionId());
        }
    }

    public void validateAtLeastOneRequiredPosition(ShiftTemplate template) {
        if (template.getRequiredPositions() == null || template.getRequiredPositions().isEmpty()) {
            throw new ShiftValidationException(
                    "Shift template must have at least one required position with required count >= 1");
        }
        boolean hasRequired = template.getRequiredPositions().stream()
                .anyMatch(stp -> stp.getRequiredCount() != null && stp.getRequiredCount() >= 1);
        if (!hasRequired) {
            throw new ShiftValidationException(
                    "Shift template must have at least one required position with required count >= 1");
        }
    }

    public void applyComputedRequiredAndIdeal(ShiftTemplate template) {
        if (template.getRequiredPositions() == null || template.getRequiredPositions().isEmpty()) {
            return;
        }
        int required = template.getRequiredPositions().stream()
                .mapToInt(ShiftTemplatePosition::getRequiredCount)
                .sum();
        int ideal = template.getRequiredPositions().stream()
                .mapToInt(stp -> stp.getIdealCount() != null ? stp.getIdealCount() : stp.getRequiredCount())
                .sum();
        template.setRequiredEmployees(required);
        template.setIdealEmployees(ideal);
    }

    public void validateIdealCount(Integer requiredCount, Integer idealCount) {
        if (idealCount != null && requiredCount != null && idealCount < requiredCount) {
            throw new ShiftValidationException(
                    "Ideal count per position must be greater than or equal to required count");
        }
    }

    public void validateTimes(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ShiftValidationException("End time must be after start time");
        }
    }

    public void validateIdealEmployees(Integer requiredEmployees, Integer idealEmployees) {
        if (idealEmployees != null && requiredEmployees != null && idealEmployees < requiredEmployees) {
            throw new ShiftValidationException(
                    "Ideal employees must be greater than or equal to required employees");
        }
    }
}
