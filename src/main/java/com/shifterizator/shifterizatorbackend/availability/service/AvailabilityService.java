package com.shifterizator.shifterizatorbackend.availability.service;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    EmployeeAvailability create(AvailabilityRequestDto dto);

    EmployeeAvailability update(Long id, AvailabilityRequestDto dto);

    void delete(Long id, boolean hardDelete);

    EmployeeAvailability findById(Long id);

    Page<EmployeeAvailability> search(Long employeeId, AvailabilityType type, Long locationId, LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable);

    List<EmployeeAvailability> findByEmployee(Long employeeId);

    List<EmployeeAvailability> findByRange(LocalDate start, LocalDate end);
}
