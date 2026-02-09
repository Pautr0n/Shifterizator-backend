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

    /**
     * Resolves and validates a Location entity by ID.
     *
     * @param locationId the location ID to resolve
     * @return the Location entity
     * @throws LocationNotFoundException if location is not found
     */
    public Location resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
    }

    /**
     * Resolves Language entities from a set of language IDs.
     *
     * @param languageIds the set of language IDs to resolve
     * @return the set of Language entities
     * @throws LanguageNotFoundException if any language is not found
     */
    public Set<Language> resolveLanguages(Set<Long> languageIds) {
        if (languageIds == null || languageIds.isEmpty()) {
            return new HashSet<>();
        }

        return languageIds.stream()
                .map(id -> languageRepository.findById(id)
                        .orElseThrow(() -> new LanguageNotFoundException("Language not found")))
                .collect(Collectors.toSet());
    }

    /**
     * Builds and assigns position requirements to a shift template.
     *
     * @param template the shift template to assign positions to
     * @param requirements the list of position requirements from DTO
     * @throws PositionNotFoundException if any position is not found
     */
    public void buildPositionRequirements(ShiftTemplate template, List<PositionRequirementDto> requirements) {
        Set<ShiftTemplatePosition> positions = new HashSet<>();
        
        for (PositionRequirementDto req : requirements) {
            Position position = positionRepository.findById(req.positionId())
                    .orElseThrow(() -> new PositionNotFoundException("Position not found: " + req.positionId()));
            
            ShiftTemplatePosition templatePosition = ShiftTemplatePosition.builder()
                    .shiftTemplate(template)
                    .position(position)
                    .requiredCount(req.requiredCount())
                    .build();
            positions.add(templatePosition);
        }
        
        template.setRequiredPositions(positions);
    }

    /**
     * Validates that end time is after start time.
     *
     * @param startTime the start time
     * @param endTime the end time
     * @throws ShiftValidationException if end time is not after start time
     */
    public void validateTimes(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ShiftValidationException("End time must be after start time");
        }
    }

    /**
     * Validates that ideal employees is not less than required employees when both are set.
     *
     * @param requiredEmployees minimum required (must be non-null when ideal is set)
     * @param idealEmployees    target when enough staff available
     * @throws ShiftValidationException if ideal is set and ideal &lt; required
     */
    public void validateIdealEmployees(Integer requiredEmployees, Integer idealEmployees) {
        if (idealEmployees != null && requiredEmployees != null && idealEmployees < requiredEmployees) {
            throw new ShiftValidationException(
                    "Ideal employees must be greater than or equal to required employees");
        }
    }
}
