package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.notification.event.ShiftAssignmentCreatedEvent;
import com.shifterizator.shifterizatorbackend.notification.event.ShiftAssignmentRemovedEvent;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentAssignResult;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAssignmentNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.advisor.ShiftAssignmentPreferenceAdvisor;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceCompletenessService;
import com.shifterizator.shifterizatorbackend.shift.service.validator.ShiftAssignmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(noRollbackFor = Exception.class)
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService {

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftAssignmentValidator shiftAssignmentValidator;
    private final ShiftInstanceCompletenessService shiftInstanceCompletenessService;
    private final ShiftAssignmentPreferenceAdvisor shiftAssignmentPreferenceAdvisor;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ShiftAssignmentAssignResult assign(ShiftAssignmentRequestDto dto) {
        ShiftInstance shiftInstance = shiftInstanceRepository.findByIdFullyLoaded(dto.shiftInstanceId())
                .orElseThrow(() -> new ShiftInstanceNotFoundException("Shift instance not found"));

        Employee employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        shiftAssignmentValidator.validateNotAlreadyAssigned(dto.shiftInstanceId(), dto.employeeId());
        shiftAssignmentValidator.validateEmployeeCompanyAndLocation(employee, shiftInstance);
        shiftAssignmentValidator.validateEmployeeAvailability(employee.getId(), shiftInstance.getDate());
        shiftAssignmentValidator.validatePositionMatch(employee, shiftInstance);
        shiftAssignmentValidator.validateLanguageRequirements(employee, shiftInstance);
        shiftAssignmentValidator.validateNoOverlappingShifts(employee.getId(), shiftInstance);
        shiftAssignmentValidator.validatePositionCapacity(employee, shiftInstance);

        ShiftAssignment assignment = ShiftAssignment.builder()
                .shiftInstance(shiftInstance)
                .employee(employee)
                .isConfirmed(false)
                .build();

        ShiftAssignment saved = shiftAssignmentRepository.save(assignment);

        shiftInstanceCompletenessService.updateCompleteness(shiftInstance);

        applicationEventPublisher.publishEvent(new ShiftAssignmentCreatedEvent(saved));

        List<String> warnings = shiftAssignmentPreferenceAdvisor.getWarnings(saved.getEmployee(), saved.getShiftInstance());
        return new ShiftAssignmentAssignResult(saved, warnings);
    }

    @Override
    public void unassign(Long shiftInstanceId, Long employeeId) {
        ShiftAssignment assignment = shiftAssignmentRepository
                .findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(shiftInstanceId, employeeId)
                .orElseThrow(() -> new ShiftAssignmentNotFoundException("Assignment not found"));

        assignment.setDeletedAt(LocalDateTime.now());

        ShiftInstance shiftInstance = assignment.getShiftInstance();
        shiftInstanceCompletenessService.updateCompleteness(shiftInstance);

        applicationEventPublisher.publishEvent(new ShiftAssignmentRemovedEvent(assignment));
    }

    @Override
    public void unassignEmployeeFromShiftsInDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<ShiftAssignment> inRange = shiftAssignmentRepository
                .findByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(employeeId, startDate, endDate);
        for (ShiftAssignment assignment : inRange) {
            assignment.setDeletedAt(LocalDateTime.now());
            shiftInstanceCompletenessService.updateCompleteness(assignment.getShiftInstance());
            applicationEventPublisher.publishEvent(new ShiftAssignmentRemovedEvent(assignment));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftAssignment findById(Long id) {
        return shiftAssignmentRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftAssignmentNotFoundException("Assignment not found"));
    }

    @Override
    public ShiftAssignment confirm(Long id) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new ShiftAssignmentNotFoundException("Assignment not found"));

        assignment.setIsConfirmed(true);
        assignment.setConfirmedAt(LocalDateTime.now());

        return shiftAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByShiftInstance(Long shiftInstanceId) {
        return shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(shiftInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignment> findByEmployee(Long employeeId) {
        return shiftAssignmentRepository
                .findByEmployee_IdAndDeletedAtIsNullOrderByShiftInstance_DateAscShiftInstance_StartTimeAsc(employeeId);
    }
}
