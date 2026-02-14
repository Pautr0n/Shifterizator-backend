package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftTemplateDomainService;
import com.shifterizator.shifterizatorbackend.shift.spec.ShiftTemplateSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftTemplateMapper shiftTemplateMapper;
    private final ShiftTemplateDomainService shiftTemplateDomainService;

    @Override
    public ShiftTemplate create(ShiftTemplateRequestDto dto) {
        Location location = shiftTemplateDomainService.resolveLocation(dto.locationId());
        shiftTemplateDomainService.validateTimes(dto.startTime(), dto.endTime());

        var languages = shiftTemplateDomainService.resolveLanguages(dto.requiredLanguageIds());
        ShiftTemplate template = shiftTemplateMapper.toEntity(dto, location, languages);

        shiftTemplateDomainService.buildPositionRequirements(template, dto.requiredPositions());
        shiftTemplateDomainService.validateIdealEmployees(template.getRequiredEmployees(), template.getIdealEmployees());

        return shiftTemplateRepository.save(template);
    }

    @Override
    public ShiftTemplate update(Long id, ShiftTemplateRequestDto dto) {
        ShiftTemplate existing = shiftTemplateRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = shiftTemplateDomainService.resolveLocation(dto.locationId());
        }

        shiftTemplateDomainService.validateTimes(dto.startTime(), dto.endTime());
        var languages = shiftTemplateDomainService.resolveLanguages(dto.requiredLanguageIds());

        existing.setLocation(location);
        existing.setStartTime(dto.startTime());
        existing.setEndTime(dto.endTime());
        existing.setDescription(dto.description());
        existing.setIdealEmployees(dto.idealEmployees());
        existing.setRequiredLanguages(languages);
        existing.setIsActive(dto.isActive() != null ? dto.isActive() : true);
        existing.setPriority(dto.priority());

        shiftTemplateDomainService.buildPositionRequirements(existing, dto.requiredPositions());
        shiftTemplateDomainService.validateIdealEmployees(existing.getRequiredEmployees(), existing.getIdealEmployees());

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
        return shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId);
    }
}
