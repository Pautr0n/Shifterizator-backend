package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
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
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final LocationRepository locationRepository;
    private final ShiftInstanceMapper shiftInstanceMapper;

    @Override
    public ShiftInstance create(ShiftInstanceRequestDto dto) {
        ShiftTemplate template = shiftTemplateRepository.findById(dto.shiftTemplateId())
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));

        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        validateTimes(dto.startTime(), dto.endTime());

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
            template = shiftTemplateRepository.findById(dto.shiftTemplateId())
                    .filter(t -> t.getDeletedAt() == null)
                    .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));
        }

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = locationRepository.findById(dto.locationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        }

        validateTimes(dto.startTime(), dto.endTime());

        existing.setShiftTemplate(template);
        existing.setLocation(location);
        existing.setDate(dto.date());
        existing.setStartTime(dto.startTime());
        existing.setEndTime(dto.endTime());
        existing.setRequiredEmployees(dto.requiredEmployees());
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

    private void validateTimes(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new ShiftValidationException("End time must be after start time");
        }
    }
}
