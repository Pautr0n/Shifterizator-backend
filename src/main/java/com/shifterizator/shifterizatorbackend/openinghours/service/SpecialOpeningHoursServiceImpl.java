package com.shifterizator.shifterizatorbackend.openinghours.service;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursNotFoundException;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursValidationException;
import com.shifterizator.shifterizatorbackend.openinghours.mapper.SpecialOpeningHoursMapper;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.repository.SpecialOpeningHoursRepository;
import com.shifterizator.shifterizatorbackend.openinghours.spec.SpecialOpeningHoursSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpecialOpeningHoursServiceImpl implements SpecialOpeningHoursService {

    private final SpecialOpeningHoursRepository openingHoursRepository;
    private final LocationRepository locationRepository;
    private final SpecialOpeningHoursMapper openingHoursMapper;

    @Override
    public SpecialOpeningHours create(SpecialOpeningHoursRequestDto dto) {
        validateTimes(dto.openTime(), dto.closeTime());
        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));

        if (Boolean.TRUE.equals(dto.appliesToCompany())) {
            return createForAllCompanyLocations(dto, location);
        } else {
            SpecialOpeningHours openingHours = openingHoursMapper.toEntity(dto, location);
            return openingHoursRepository.save(openingHours);
        }
    }

    private SpecialOpeningHours createForAllCompanyLocations(SpecialOpeningHoursRequestDto dto, Location location) {
        List<Location> companyLocations = locationRepository.findByCompany_Id(location.getCompany().getId());
        if (companyLocations.isEmpty()) {
            throw new SpecialOpeningHoursValidationException("No locations found for the company");
        }

        SpecialOpeningHours firstCreated = null;
        for (Location loc : companyLocations) {
            SpecialOpeningHoursRequestDto dtoForLocation = new SpecialOpeningHoursRequestDto(
                    loc.getId(),
                    dto.date(),
                    dto.openTime(),
                    dto.closeTime(),
                    dto.reason(),
                    dto.colorCode(),
                    false // Each individual row has appliesToCompany=false
            );
            SpecialOpeningHours openingHours = openingHoursMapper.toEntity(dtoForLocation, loc);
            SpecialOpeningHours saved = openingHoursRepository.save(openingHours);
            if (firstCreated == null) {
                firstCreated = saved;
            }
        }
        return firstCreated; // Return the first created one
    }

    @Override
    public SpecialOpeningHours update(Long id, SpecialOpeningHoursRequestDto dto) {
        validateTimes(dto.openTime(), dto.closeTime());
        SpecialOpeningHours existing = openingHoursRepository.findById(id)
                .filter(oh -> oh.getDeletedAt() == null)
                .orElseThrow(() -> new SpecialOpeningHoursNotFoundException("Special opening hours not found"));

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = locationRepository.findById(dto.locationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        }

        existing.setLocation(location);
        existing.setDate(dto.date());
        existing.setOpenTime(dto.openTime());
        existing.setCloseTime(dto.closeTime());
        existing.setReason(dto.reason());
        existing.setColorCode(dto.colorCode());
        existing.setAppliesToCompany(dto.appliesToCompany() != null ? dto.appliesToCompany() : false);
        return existing;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {
        SpecialOpeningHours openingHours = openingHoursRepository.findById(id)
                .orElseThrow(() -> new SpecialOpeningHoursNotFoundException("Special opening hours not found"));

        if (hardDelete) {
            openingHoursRepository.delete(openingHours);
        } else {
            openingHours.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialOpeningHours findById(Long id) {
        return openingHoursRepository.findById(id)
                .filter(oh -> oh.getDeletedAt() == null)
                .orElseThrow(() -> new SpecialOpeningHoursNotFoundException("Special opening hours not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SpecialOpeningHours> search(Long locationId, Long companyId, Pageable pageable) {
        Specification<SpecialOpeningHours> spec = SpecialOpeningHoursSpecs.notDeleted();
        if (locationId != null) {
            spec = spec.and(SpecialOpeningHoursSpecs.byLocation(locationId));
        }
        if (companyId != null) {
            spec = spec.and(SpecialOpeningHoursSpecs.byCompany(companyId));
        }
        return openingHoursRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialOpeningHours> findByLocation(Long locationId) {
        return openingHoursRepository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialOpeningHours> findByLocationAndMonth(Long locationId, YearMonth yearMonth) {
        Specification<SpecialOpeningHours> spec = SpecialOpeningHoursSpecs.notDeleted()
                .and(SpecialOpeningHoursSpecs.byLocation(locationId))
                .and(SpecialOpeningHoursSpecs.inMonth(yearMonth));
        return openingHoursRepository.findAll(spec);
    }

    private void validateTimes(LocalTime openTime, LocalTime closeTime) {
        if (!closeTime.isAfter(openTime)) {
            throw new SpecialOpeningHoursValidationException("Close time must be after open time");
        }
    }
}
