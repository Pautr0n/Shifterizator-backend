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
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
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

    public void buildPositionRequirements(ShiftTemplate template, List<PositionRequirementDto> requirements) {
        Set<ShiftTemplatePosition> positions = new HashSet<>();

        for (PositionRequirementDto req : requirements) {
            validateIdealCount(req.requiredCount(), req.idealCount());
            Position position = positionRepository.findById(req.positionId())
                    .orElseThrow(() -> new PositionNotFoundException("Position not found: " + req.positionId()));

            ShiftTemplatePosition templatePosition = ShiftTemplatePosition.builder()
                    .shiftTemplate(template)
                    .position(position)
                    .requiredCount(req.requiredCount())
                    .idealCount(req.idealCount())
                    .build();
            positions.add(templatePosition);
        }

        template.setRequiredPositions(positions);
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
