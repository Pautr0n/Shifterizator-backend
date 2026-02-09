package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAssignmentNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceCompletenessService;
import com.shifterizator.shifterizatorbackend.shift.service.validator.ShiftAssignmentValidator;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftAssignmentServiceImplTest {

    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Mock
    private ShiftInstanceRepository shiftInstanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftAssignmentValidator shiftAssignmentValidator;

    @Mock
    private ShiftInstanceCompletenessService shiftInstanceCompletenessService;

    @InjectMocks
    private ShiftAssignmentServiceImpl service;

    private static LocalDate futureDate() {
        return LocalDate.now().plusDays(10);
    }

    private ShiftInstance createShiftInstance() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Position position1 = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Position position2 = Position.builder().id(2L).name("Manager").company(company).build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(1L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();

        ShiftTemplatePosition stp1 = ShiftTemplatePosition.builder()
                .shiftTemplate(template)
                .position(position1)
                .requiredCount(2)
                .build();
        ShiftTemplatePosition stp2 = ShiftTemplatePosition.builder()
                .shiftTemplate(template)
                .position(position2)
                .requiredCount(1)
                .build();

        template.setRequiredPositions(Set.of(stp1, stp2));

        return ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(futureDate())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(3)
                .isComplete(false)
                .build();
    }

    @Test
    void assign_shouldAssignEmployeeSuccessfully() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(shiftInstance)
                .employee(employee)
                .isConfirmed(false)
                .build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftAssignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);
        // Validator passes all validations (no exceptions thrown)
        doNothing().when(shiftAssignmentValidator).validateNotAlreadyAssigned(any(), any());
        doNothing().when(shiftAssignmentValidator).validateEmployeeAvailability(any(), any());
        doNothing().when(shiftAssignmentValidator).validatePositionMatch(any(), any());
        doNothing().when(shiftAssignmentValidator).validateLanguageRequirements(any(), any());
        doNothing().when(shiftAssignmentValidator).validateNoOverlappingShifts(any(), any());
        doNothing().when(shiftAssignmentValidator).validatePositionCapacity(any(), any());
        doNothing().when(shiftInstanceCompletenessService).updateCompleteness(any());

        ShiftAssignment result = service.assign(dto);

        assertThat(result.getId()).isEqualTo(100L);
        verify(shiftAssignmentRepository).save(any(ShiftAssignment.class));
        verify(shiftAssignmentValidator).validateNotAlreadyAssigned(99L, 1L);
        verify(shiftAssignmentValidator).validateEmployeeAvailability(1L, futureDate());
        verify(shiftAssignmentValidator).validatePositionMatch(employee, shiftInstance);
        verify(shiftAssignmentValidator).validateLanguageRequirements(employee, shiftInstance);
        verify(shiftAssignmentValidator).validateNoOverlappingShifts(1L, shiftInstance);
        verify(shiftAssignmentValidator).validatePositionCapacity(employee, shiftInstance);
        verify(shiftInstanceCompletenessService).updateCompleteness(shiftInstance);
    }

    @Test
    void assign_shouldThrowWhenShiftInstanceNotFound() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(999L, 1L);

        when(shiftInstanceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ShiftInstanceNotFoundException.class)
                .hasMessageContaining("Shift instance not found");
    }

    @Test
    void assign_shouldThrowWhenEmployeeNotFound() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 999L);
        ShiftInstance shiftInstance = createShiftInstance();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void assign_shouldThrowWhenAlreadyAssigned() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        ShiftAssignment existing = ShiftAssignment.builder().id(100L).build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doThrow(new ShiftValidationException("Employee is already assigned to this shift"))
                .when(shiftAssignmentValidator).validateNotAlreadyAssigned(99L, 1L);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void assign_shouldThrowWhenEmployeeOnVacation() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(shiftAssignmentValidator).validateNotAlreadyAssigned(any(), any());
        doThrow(new ShiftValidationException("Employee is marked as VACATION on this date"))
                .when(shiftAssignmentValidator).validateEmployeeAvailability(1L, futureDate());

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("VACATION");
    }

    @Test
    void assign_shouldThrowWhenPositionDoesNotMatch() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position wrongPosition = Position.builder().id(999L).name("Cashier").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(wrongPosition).build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        doNothing().when(shiftAssignmentValidator).validateNotAlreadyAssigned(any(), any());
        doNothing().when(shiftAssignmentValidator).validateEmployeeAvailability(any(), any());
        doThrow(new ShiftValidationException("Employee position does not match any required position for this shift template"))
                .when(shiftAssignmentValidator).validatePositionMatch(employee, shiftInstance);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("position does not match");
    }

    @Test
    void assign_shouldThrowWhenPositionCapacityReached() {
        ShiftAssignmentRequestDto dto = new ShiftAssignmentRequestDto(99L, 1L);
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee1 = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        when(shiftInstanceRepository.findById(99L)).thenReturn(Optional.of(shiftInstance));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee1));
        doNothing().when(shiftAssignmentValidator).validateNotAlreadyAssigned(any(), any());
        doNothing().when(shiftAssignmentValidator).validateEmployeeAvailability(any(), any());
        doNothing().when(shiftAssignmentValidator).validatePositionMatch(any(), any());
        doNothing().when(shiftAssignmentValidator).validateLanguageRequirements(any(), any());
        doNothing().when(shiftAssignmentValidator).validateNoOverlappingShifts(any(), any());
        doThrow(new ShiftValidationException("Position capacity reached: 1 employees already assigned (required: 2)"))
                .when(shiftAssignmentValidator).validatePositionCapacity(employee1, shiftInstance);

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ShiftValidationException.class)
                .hasMessageContaining("capacity reached");
    }

    @Test
    void unassign_shouldUnassignEmployee() {
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(shiftInstance)
                .employee(employee)
                .build();

        when(shiftAssignmentRepository.findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(99L, 1L))
                .thenReturn(Optional.of(assignment));
        doNothing().when(shiftInstanceCompletenessService).updateCompleteness(any());

        service.unassign(99L, 1L);

        assertThat(assignment.getDeletedAt()).isNotNull();
        verify(shiftInstanceCompletenessService).updateCompleteness(shiftInstance);
    }

    @Test
    void unassign_shouldThrowWhenAssignmentNotFound() {
        when(shiftAssignmentRepository.findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(99L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unassign(99L, 1L))
                .isInstanceOf(ShiftAssignmentNotFoundException.class)
                .hasMessageContaining("Assignment not found");
    }

    @Test
    void findById_shouldReturnAssignment() {
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(shiftInstance)
                .employee(employee)
                .build();

        when(shiftAssignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));

        ShiftAssignment result = service.findById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findByShiftInstance_shouldReturnList() {
        ShiftInstance shiftInstance = createShiftInstance();
        Company company = shiftInstance.getLocation().getCompany();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(shiftInstance)
                .employee(employee)
                .build();

        when(shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(99L))
                .thenReturn(List.of(assignment));

        List<ShiftAssignment> result = service.findByShiftInstance(99L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }
}
