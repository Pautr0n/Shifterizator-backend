package com.shifterizator.shifterizatorbackend.blackoutdays.service;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayNotFoundException;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayValidationException;
import com.shifterizator.shifterizatorbackend.blackoutdays.mapper.BlackoutDayMapper;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.repository.BlackoutDayRepository;
import com.shifterizator.shifterizatorbackend.blackoutdays.spec.BlackoutDaySpecs;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BlackoutDayServiceImpl implements BlackoutDayService {

    private final BlackoutDayRepository blackoutDayRepository;
    private final LocationRepository locationRepository;
    private final BlackoutDayMapper blackoutDayMapper;

    @Override
    public BlackoutDay create(BlackoutDayRequestDto dto) {
        Location location = locationRepository.findById(dto.locationId())
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        ensureNoOverlapWithSpecialOpeningHours(dto.locationId(), dto.date());

        if (Boolean.TRUE.equals(dto.appliesToCompany())) {
            return createForAllCompanyLocations(dto, location);
        } else {
            BlackoutDay blackoutDay = blackoutDayMapper.toEntity(dto, location);
            return blackoutDayRepository.save(blackoutDay);
        }
    }

    private BlackoutDay createForAllCompanyLocations(BlackoutDayRequestDto dto, Location location) {
        List<Location> companyLocations = locationRepository.findByCompany_Id(location.getCompany().getId());
        if (companyLocations.isEmpty()) {
            throw new BlackoutDayValidationException("No locations found for the company");
        }

        BlackoutDay firstCreated = null;
        for (Location loc : companyLocations) {
            ensureNoOverlapWithSpecialOpeningHours(loc.getId(), dto.date());
            BlackoutDayRequestDto dtoForLocation = new BlackoutDayRequestDto(
                    loc.getId(),
                    dto.date(),
                    dto.reason(),
                    false // Each individual row has appliesToCompany=false
            );
            BlackoutDay blackoutDay = blackoutDayMapper.toEntity(dtoForLocation, loc);
            BlackoutDay saved = blackoutDayRepository.save(blackoutDay);
            if (firstCreated == null) {
                firstCreated = saved;
            }
        }
        return firstCreated;
    }

    @Override
    public BlackoutDay update(Long id, BlackoutDayRequestDto dto) {
        BlackoutDay existing = blackoutDayRepository.findById(id)
                .filter(bd -> bd.getDeletedAt() == null)
                .orElseThrow(() -> new BlackoutDayNotFoundException("Blackout day not found"));

        Location location = existing.getLocation();
        if (!location.getId().equals(dto.locationId())) {
            location = locationRepository.findById(dto.locationId())
                    .orElseThrow(() -> new LocationNotFoundException("Location not found"));
        }

        if (!existing.getDate().equals(dto.date()) || !existing.getLocation().getId().equals(dto.locationId())) {
            ensureNoOverlapWithSpecialOpeningHours(dto.locationId(), dto.date());
        }

        existing.setLocation(location);
        existing.setDate(dto.date());
        existing.setReason(dto.reason());
        existing.setAppliesToCompany(dto.appliesToCompany() != null ? dto.appliesToCompany() : false);
        return existing;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {
        BlackoutDay blackoutDay = blackoutDayRepository.findById(id)
                .orElseThrow(() -> new BlackoutDayNotFoundException("Blackout day not found"));

        if (hardDelete) {
            blackoutDayRepository.delete(blackoutDay);
        } else {
            blackoutDay.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BlackoutDay findById(Long id) {
        return blackoutDayRepository.findById(id)
                .filter(bd -> bd.getDeletedAt() == null)
                .orElseThrow(() -> new BlackoutDayNotFoundException("Blackout day not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlackoutDay> search(Long locationId, Long companyId, Pageable pageable) {
        Specification<BlackoutDay> spec = BlackoutDaySpecs.notDeleted();
        if (locationId != null) {
            spec = spec.and(BlackoutDaySpecs.byLocation(locationId));
        }
        if (companyId != null) {
            spec = spec.and(BlackoutDaySpecs.byCompany(companyId));
        }
        return blackoutDayRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlackoutDay> findByLocation(Long locationId) {
        return blackoutDayRepository.findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlackoutDay> findByLocationAndMonth(Long locationId, YearMonth yearMonth) {
        Specification<BlackoutDay> spec = BlackoutDaySpecs.notDeleted()
                .and(BlackoutDaySpecs.byLocation(locationId))
                .and(BlackoutDaySpecs.inMonth(yearMonth));
        return blackoutDayRepository.findAll(spec);
    }

    private void ensureNoOverlapWithSpecialOpeningHours(Long locationId, java.time.LocalDate date) {
        if (blackoutDayRepository.existsSpecialOpeningHoursForLocationAndDate(locationId, date)) {
            throw new BlackoutDayValidationException("A special opening hours record already exists for this location and date");
        }
    }
}
