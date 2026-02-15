package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceDomainService;
import com.shifterizator.shifterizatorbackend.shift.spec.ShiftInstanceSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftInstanceServiceImpl implements ShiftInstanceService {

    private final ShiftInstanceRepository shiftInstanceRepository;
    private final ShiftInstanceMapper shiftInstanceMapper;
    private final ShiftInstanceDomainService shiftInstanceDomainService;

    @Override
    public ShiftInstance create(ShiftInstanceRequestDto dto) {
        ShiftTemplate template = shiftInstanceDomainService.resolveTemplate(dto.shiftTemplateId());
        Location location = shiftInstanceDomainService.resolveLocation(dto.locationId());
        shiftInstanceDomainService.validateTimes(dto.startTime(), dto.endTime());
        shiftInstanceDomainService.validateIdealEmployees(dto.requiredEmployees(), dto.idealEmployees());

        List<ShiftInstance> existingSame = shiftInstanceRepository
                .findByLocation_IdAndDateAndShiftTemplate_IdAndDeletedAtIsNull(
                        dto.locationId(), dto.date(), dto.shiftTemplateId());
        if (!existingSame.isEmpty()) {
            throw new ShiftAlreadyExistsException(
                    "A shift with this template already exists for this date. Delete the existing shift or choose another date.");
        }

        ShiftInstance instance = shiftInstanceMapper.toEntity(dto, template, location);
        return shiftInstanceRepository.save(instance);
    }

    @Override
    public ShiftInstance update(Long id, ShiftInstanceRequestDto dto) {
        ShiftInstance existing = shiftInstanceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftInstanceNotFoundException("Shift instance not found"));

        ShiftTemplate template = existing.getShiftTemplate();
        if (!template.getId().equals(dto.shiftTemplateId())) {
            template = shiftInstanceDomainService.resolveTemplate(dto.shiftTemplateId());
        }

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = shiftInstanceDomainService.resolveLocation(dto.locationId());
        }

        shiftInstanceDomainService.validateTimes(dto.startTime(), dto.endTime());
        shiftInstanceDomainService.validateIdealEmployees(dto.requiredEmployees(), dto.idealEmployees());

        existing.setShiftTemplate(template);
        existing.setLocation(location);
        existing.setDate(dto.date());
        existing.setStartTime(dto.startTime());
        existing.setEndTime(dto.endTime());
        // requiredEmployees and idealEmployees are read-only (computed from template required positions)
        existing.setNotes(dto.notes());

        return existing;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {
        ShiftInstance instance = shiftInstanceRepository.findById(id)
                .orElseThrow(() -> new ShiftInstanceNotFoundException("Shift instance not found"));

        if (hardDelete) {
            shiftInstanceRepository.delete(instance);
        } else {
            instance.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            shiftInstanceRepository.findById(id)
                    .filter(i -> i.getDeletedAt() == null)
                    .ifPresent(i -> i.setDeletedAt(now));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftInstance findById(Long id) {
        return shiftInstanceRepository.findById(id)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftInstanceNotFoundException("Shift instance not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftInstance> search(Long locationId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<ShiftInstance> spec = ShiftInstanceSpecs.notDeleted();
        if (locationId != null) {
            spec = spec.and(ShiftInstanceSpecs.byLocation(locationId));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(ShiftInstanceSpecs.inDateRange(startDate, endDate));
        }
        return shiftInstanceRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftInstance> findByLocationAndDate(Long locationId, LocalDate date) {
        return shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(locationId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftInstance> findByLocationAndDateRange(Long locationId, LocalDate startDate, LocalDate endDate) {
        return shiftInstanceRepository.findByLocation_IdAndDateBetweenAndDeletedAtIsNullOrderByDateAscStartTimeAsc(locationId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public int getAssignedCount(Long shiftInstanceId) {
        return shiftInstanceRepository.countActiveAssignments(shiftInstanceId);
    }
}
