package com.shifterizator.shifterizatorbackend.availability.service;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityNotFoundException;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityValidationException;
import com.shifterizator.shifterizatorbackend.availability.exception.OverlappingAvailabilityException;
import com.shifterizator.shifterizatorbackend.availability.mapper.AvailabilityMapper;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.availability.spec.AvailabilitySpecs;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Set<AvailabilityType> BLOCKING_TYPES = Set.of(
            AvailabilityType.VACATION,
            AvailabilityType.SICK_LEAVE,
            AvailabilityType.UNAVAILABLE,
            AvailabilityType.UNJUSTIFIED_ABSENCE,
            AvailabilityType.PERSONAL_LEAVE
    );

    private final EmployeeAvailabilityRepository availabilityRepository;
    private final EmployeeRepository employeeRepository;
    private final AvailabilityMapper availabilityMapper;
    private final ShiftAssignmentService shiftAssignmentService;

    @Override
    public EmployeeAvailability create(AvailabilityRequestDto dto) {
        validateDates(dto.startDate(), dto.endDate());
        validateNotInPast(dto.startDate());
        Employee employee = employeeRepository.findActiveById(dto.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        ensureNoOverlap(dto.employeeId(), dto.startDate(), dto.endDate(), null);

        EmployeeAvailability availability = availabilityMapper.toEntity(dto, employee);
        EmployeeAvailability saved = availabilityRepository.save(availability);
        if (BLOCKING_TYPES.contains(saved.getType())) {
            shiftAssignmentService.unassignEmployeeFromShiftsInDateRange(
                    saved.getEmployee().getId(), saved.getStartDate(), saved.getEndDate());
        }
        return saved;
    }

    @Override
    public EmployeeAvailability update(Long id, AvailabilityRequestDto dto) {
        validateDates(dto.startDate(), dto.endDate());
        validateNotInPast(dto.startDate());
        EmployeeAvailability existing = availabilityRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found"));
        ensureNoOverlap(dto.employeeId(), dto.startDate(), dto.endDate(), id);

        Employee employee = existing.getEmployee();
        if (!employee.getId().equals(dto.employeeId())) {
            employee = employeeRepository.findActiveById(dto.employeeId())
                    .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        }

        existing.setEmployee(employee);
        existing.setStartDate(dto.startDate());
        existing.setEndDate(dto.endDate());
        existing.setType(dto.type());
        if (BLOCKING_TYPES.contains(existing.getType())) {
            shiftAssignmentService.unassignEmployeeFromShiftsInDateRange(
                    existing.getEmployee().getId(), existing.getStartDate(), existing.getEndDate());
        }
        return existing;
    }

    @Override
    public void delete(Long id, boolean hardDelete) {
        EmployeeAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found"));

        if (hardDelete) {
            availabilityRepository.delete(availability);
        } else {
            availability.setDeletedAt(LocalDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAvailability findById(Long id) {
        return availabilityRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeAvailability> search(Long employeeId, AvailabilityType type, Long locationId, LocalDate rangeStart, LocalDate rangeEnd, Pageable pageable) {
        Specification<EmployeeAvailability> spec = AvailabilitySpecs.notDeleted();
        if (employeeId != null) {
            spec = spec.and(AvailabilitySpecs.byEmployee(employeeId));
        }
        if (type != null) {
            spec = spec.and(AvailabilitySpecs.byType(type));
        }
        if (locationId != null) {
            spec = spec.and(AvailabilitySpecs.byEmployeeLocation(locationId));
        }
        if (rangeStart != null && rangeEnd != null) {
            spec = spec.and(AvailabilitySpecs.inDateRange(rangeStart, rangeEnd));
        }
        return availabilityRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAvailability> findByEmployee(Long employeeId) {
        return availabilityRepository.findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAvailability> findByRange(LocalDate start, LocalDate end) {
        Specification<EmployeeAvailability> spec = AvailabilitySpecs.notDeleted()
                .and(AvailabilitySpecs.inDateRange(start, end));
        return availabilityRepository.findAll(spec);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new AvailabilityValidationException("End date must be on or after start date");
        }
    }

    private void validateNotInPast(LocalDate startDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new AvailabilityValidationException("Availability cannot start in the past");
        }
    }

    private void ensureNoOverlap(Long employeeId, LocalDate start, LocalDate end, Long excludeId) {
        List<EmployeeAvailability> overlapping = availabilityRepository.findOverlapping(employeeId, start, end, excludeId);
        if (!overlapping.isEmpty()) {
            throw new OverlappingAvailabilityException("Another availability record overlaps with this date range for the same employee");
        }
    }
}
