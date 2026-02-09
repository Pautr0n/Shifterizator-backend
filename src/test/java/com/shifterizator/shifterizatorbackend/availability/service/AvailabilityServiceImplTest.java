package com.shifterizator.shifterizatorbackend.availability.service;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityNotFoundException;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityValidationException;
import com.shifterizator.shifterizatorbackend.availability.exception.OverlappingAvailabilityException;
import com.shifterizator.shifterizatorbackend.availability.mapper.AvailabilityMapper;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftAssignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private EmployeeAvailabilityRepository availabilityRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AvailabilityMapper availabilityMapper;
    @Mock
    private ShiftAssignmentService shiftAssignmentService;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private static LocalDate futureStart() {
        return LocalDate.now().plusDays(1);
    }

    private static LocalDate futureEnd() {
        return LocalDate.now().plusDays(10);
    }

    @Test
    void create_shouldCreateSuccessfully() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, futureStart(), futureEnd(), AvailabilityType.VACATION);
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").build();
        EmployeeAvailability entity = EmployeeAvailability.builder()
                .employee(employee)
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .type(dto.type())
                .build();
        EmployeeAvailability saved = EmployeeAvailability.builder().id(99L).employee(employee)
                .startDate(dto.startDate()).endDate(dto.endDate()).type(dto.type()).build();

        when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findOverlapping(eq(1L), eq(dto.startDate()), eq(dto.endDate()), eq(null)))
                .thenReturn(List.of());
        when(availabilityMapper.toEntity(dto, employee)).thenReturn(entity);
        when(availabilityRepository.save(any(EmployeeAvailability.class))).thenReturn(saved);

        EmployeeAvailability result = availabilityService.create(dto);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getType()).isEqualTo(AvailabilityType.VACATION);
        verify(availabilityRepository).save(any(EmployeeAvailability.class));
        verify(shiftAssignmentService).unassignEmployeeFromShiftsInDateRange(1L, dto.startDate(), dto.endDate());
    }

    @Test
    void create_shouldThrowWhenEndDateBeforeStartDate() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, futureStart(), futureStart().minusDays(1), AvailabilityType.VACATION);

        assertThatThrownBy(() -> availabilityService.create(dto))
                .isInstanceOf(AvailabilityValidationException.class)
                .hasMessage("End date must be on or after start date");
        verify(employeeRepository, never()).findActiveById(any());
    }

    @Test
    void create_shouldThrowWhenStartDateInPast() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), AvailabilityType.VACATION);

        assertThatThrownBy(() -> availabilityService.create(dto))
                .isInstanceOf(AvailabilityValidationException.class)
                .hasMessage("Availability cannot start in the past");
        verify(employeeRepository, never()).findActiveById(any());
    }

    @Test
    void create_shouldThrowWhenEmployeeNotFound() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(999L, futureStart(), futureEnd(), AvailabilityType.VACATION);
        when(employeeRepository.findActiveById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.create(dto))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowWhenOverlapping() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, futureStart(), futureEnd(), AvailabilityType.VACATION);
        Employee employee = Employee.builder().id(1L).build();
        when(employeeRepository.findActiveById(1L)).thenReturn(Optional.of(employee));
        when(availabilityRepository.findOverlapping(eq(1L), eq(dto.startDate()), eq(dto.endDate()), eq(null)))
                .thenReturn(List.of(EmployeeAvailability.builder().id(1L).build()));

        assertThatThrownBy(() -> availabilityService.create(dto))
                .isInstanceOf(OverlappingAvailabilityException.class)
                .hasMessageContaining("overlaps");
        verify(availabilityRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        Long id = 99L;
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, futureStart(), futureEnd(), AvailabilityType.SICK_LEAVE);
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").build();
        EmployeeAvailability existing = EmployeeAvailability.builder().id(id).employee(employee)
                .startDate(futureStart().minusDays(5)).endDate(futureEnd().minusDays(5)).type(AvailabilityType.VACATION).build();

        when(availabilityRepository.findById(id)).thenReturn(Optional.of(existing));
        when(availabilityRepository.findOverlapping(eq(1L), eq(dto.startDate()), eq(dto.endDate()), eq(id)))
                .thenReturn(List.of());

        EmployeeAvailability result = availabilityService.update(id, dto);

        assertThat(result.getStartDate()).isEqualTo(dto.startDate());
        assertThat(result.getEndDate()).isEqualTo(dto.endDate());
        assertThat(result.getType()).isEqualTo(AvailabilityType.SICK_LEAVE);
        verify(availabilityRepository, never()).save(any());
        verify(shiftAssignmentService).unassignEmployeeFromShiftsInDateRange(1L, dto.startDate(), dto.endDate());
    }

    @Test
    void update_shouldThrowWhenAvailabilityNotFound() {
        when(availabilityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.update(999L,
                new AvailabilityRequestDto(1L, futureStart(), futureEnd(), AvailabilityType.VACATION)))
                .isInstanceOf(AvailabilityNotFoundException.class)
                .hasMessage("Availability not found");
    }

    @Test
    void update_shouldThrowWhenStartDateInPast() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(1L, LocalDate.now().minusDays(1), LocalDate.now(), AvailabilityType.VACATION);

        assertThatThrownBy(() -> availabilityService.update(99L, dto))
                .isInstanceOf(AvailabilityValidationException.class)
                .hasMessage("Availability cannot start in the past");
    }

    @Test
    void delete_shouldSoftDeleteWhenHardDeleteFalse() {
        EmployeeAvailability availability = EmployeeAvailability.builder().id(99L).build();
        when(availabilityRepository.findById(99L)).thenReturn(Optional.of(availability));

        availabilityService.delete(99L, false);

        assertThat(availability.getDeletedAt()).isNotNull();
        verify(availabilityRepository, never()).delete(any(EmployeeAvailability.class));
    }

    @Test
    void delete_shouldHardDeleteWhenHardDeleteTrue() {
        EmployeeAvailability availability = EmployeeAvailability.builder().id(99L).build();
        when(availabilityRepository.findById(99L)).thenReturn(Optional.of(availability));

        availabilityService.delete(99L, true);

        verify(availabilityRepository).delete(availability);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(availabilityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.delete(999L, false))
                .isInstanceOf(AvailabilityNotFoundException.class)
                .hasMessage("Availability not found");
    }

    @Test
    void findById_shouldReturnAvailability() {
        EmployeeAvailability availability = EmployeeAvailability.builder().id(99L).build();
        when(availabilityRepository.findById(99L)).thenReturn(Optional.of(availability));

        EmployeeAvailability result = availabilityService.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(availabilityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.findById(999L))
                .isInstanceOf(AvailabilityNotFoundException.class)
                .hasMessage("Availability not found");
    }

    @Test
    void findById_shouldThrowWhenDeleted() {
        EmployeeAvailability availability = EmployeeAvailability.builder().id(99L).deletedAt(LocalDateTime.now()).build();
        when(availabilityRepository.findById(99L)).thenReturn(Optional.of(availability));

        assertThatThrownBy(() -> availabilityService.findById(99L))
                .isInstanceOf(AvailabilityNotFoundException.class)
                .hasMessage("Availability not found");
    }

    @Test
    void findByEmployee_shouldDelegateToRepository() {
        Employee employee = Employee.builder().id(1L).build();
        EmployeeAvailability av = EmployeeAvailability.builder().id(1L).employee(employee).build();
        when(availabilityRepository.findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(1L)).thenReturn(List.of(av));

        var result = availabilityService.findByEmployee(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(availabilityRepository).findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(1L);
    }

    @Test
    void findByRange_shouldDelegateToRepository() {
        when(availabilityRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var result = availabilityService.findByRange(futureStart(), futureEnd());

        assertThat(result).isEmpty();
        verify(availabilityRepository).findAll(any(Specification.class));
    }

    @Test
    void search_shouldReturnPagedResults() {
        when(availabilityRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10))))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        var result = availabilityService.search(null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        verify(availabilityRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }
}
