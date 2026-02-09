package com.shifterizator.shifterizatorbackend.shift.service.domain;

import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ShiftInstanceDomainService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final LocationRepository locationRepository;

    /**
     * Resolves and validates a ShiftTemplate entity by ID.
     * Only returns active, non-deleted templates.
     *
     * @param templateId the template ID to resolve
     * @return the ShiftTemplate entity
     * @throws ShiftTemplateNotFoundException if template is not found or deleted
     */
    public ShiftTemplate resolveTemplate(Long templateId) {
        return shiftTemplateRepository.findById(templateId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));
    }

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
}
