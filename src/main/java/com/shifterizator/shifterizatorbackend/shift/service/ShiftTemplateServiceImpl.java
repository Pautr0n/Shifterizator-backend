package com.shifterizator.shifterizatorbackend.shift.service;

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
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.shift.spec.ShiftTemplateSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final LocationRepository locationRepository;
    private final PositionRepository positionRepository;
    private final LanguageRepository languageRepository;
    private final ShiftTemplateMapper shiftTemplateMapper;

    @Override
    public ShiftTemplate create(ShiftTemplateRequestDto dto) {
        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        validateTimes(dto.startTime(), dto.endTime());

        Set<Language> languages = new HashSet<>();
        if (dto.requiredLanguageIds() != null && !dto.requiredLanguageIds().isEmpty()) {
            languages = dto.requiredLanguageIds().stream()
                    .map(id -> languageRepository.findById(id)
                            .orElseThrow(() -> new LanguageNotFoundException("Language not found")))
                    .collect(Collectors.toSet());
        }

        ShiftTemplate template = shiftTemplateMapper.toEntity(dto, location, languages);

        // Create position requirements
        Set<ShiftTemplatePosition> positions = new HashSet<>();
        for (PositionRequirementDto req : dto.requiredPositions()) {
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

        return shiftTemplateRepository.save(template);
    }

    @Override
    public ShiftTemplate update(Long id, ShiftTemplateRequestDto dto) {
        ShiftTemplate existing = shiftTemplateRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = locationRepository.findById(dto.locationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        }

        validateTimes(dto.startTime(), dto.endTime());

        Set<Language> languages = new HashSet<>();
        if (dto.requiredLanguageIds() != null && !dto.requiredLanguageIds().isEmpty()) {
            languages = dto.requiredLanguageIds().stream()
                    .map(langId -> languageRepository.findById(langId)
                            .orElseThrow(() -> new LanguageNotFoundException("Language not found")))
                    .collect(Collectors.toSet());
        }

        existing.setLocation(location);
        existing.setStartTime(dto.startTime());
        existing.setEndTime(dto.endTime());
        existing.setDescription(dto.description());
        existing.setRequiredLanguages(languages);
        existing.setIsActive(dto.isActive() != null ? dto.isActive() : true);

        // Update position requirements - clear existing and add new ones
        existing.getRequiredPositions().clear();
        for (PositionRequirementDto req : dto.requiredPositions()) {
            Position position = positionRepository.findById(req.positionId())
                    .orElseThrow(() -> new PositionNotFoundException("Position not found: " + req.positionId()));
            
            ShiftTemplatePosition templatePosition = ShiftTemplatePosition.builder()
                    .shiftTemplate(existing)
                    .position(position)
                    .requiredCount(req.requiredCount())
                    .build();
            existing.getRequiredPositions().add(templatePosition);
        }

        return existing;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {
        ShiftTemplate template = shiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));

        if (hardDelete) {
            shiftTemplateRepository.delete(template);
        } else {
            template.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftTemplate findById(Long id) {
        return shiftTemplateRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftTemplate> search(Long locationId, Long positionId, Pageable pageable) {
        Specification<ShiftTemplate> spec = ShiftTemplateSpecs.notDeleted();
        if (locationId != null) {
            spec = spec.and(ShiftTemplateSpecs.byLocation(locationId));
        }
        if (positionId != null) {
            spec = spec.and(ShiftTemplateSpecs.byPosition(positionId));
        }
        return shiftTemplateRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftTemplate> findByLocation(Long locationId) {
        return shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(locationId);
    }

    private void validateTimes(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ShiftValidationException("End time must be after start time");
        }
    }
}
