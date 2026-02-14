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

    public ShiftTemplate resolveTemplate(Long templateId) {
        return shiftTemplateRepository.findById(templateId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftTemplateNotFoundException("Shift template not found"));
    }

    public Location resolveLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location not found"));
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
