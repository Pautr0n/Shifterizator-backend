package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.availability.repository.EmployeeAvailabilityRepository;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentAssignResult;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ScheduleDaySkippedException;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftAssignmentRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.advisor.ShiftCandidateTierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ShiftSchedulerServiceImplTest {

    @Mock
    private ShiftInstanceRepository shiftInstanceRepository;
    @Mock
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeAvailabilityRepository employeeAvailabilityRepository;
    @Mock
    private ShiftAssignmentService shiftAssignmentService;
    @Mock
    private ShiftCandidateTierService shiftCandidateTierService;
    @Mock
    private LocationService locationService;

    @InjectMocks
    private ShiftSchedulerServiceImpl scheduler;

    private static Location location(long id) {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        return Location.builder().id(id).name("HQ").address("Main").company(company).build();
    }

    @Test
    void scheduleDay_shouldThrowWhenNoShiftInstances() {
        Location loc = location(1L);
        when(locationService.findById(1L)).thenReturn(loc);
        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(1L, LocalDate.of(2025, 2, 10)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> scheduler.scheduleDay(1L, LocalDate.of(2025, 2, 10)))
                .isInstanceOf(ScheduleDaySkippedException.class)
                .hasMessageContaining("No shifts defined");

        verify(employeeRepository, never()).findActiveByLocationIdWithShiftPreferences(any());
        verify(shiftAssignmentService, never()).assign(any());
    }

    @Test
    void scheduleDay_shouldThrowWhenNoCandidates() {
        Location loc = location(1L);
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        ShiftInstance instance = ShiftInstance.builder()
                .id(10L)
                .location(loc)
                .shiftTemplate(template)
                .date(LocalDate.of(2025, 2, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(2)
                .build();

        when(locationService.findById(1L)).thenReturn(loc);
        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(1L, LocalDate.of(2025, 2, 10)))
                .thenReturn(List.of(instance));
        when(employeeRepository.findActiveByLocationIdWithShiftPreferences(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> scheduler.scheduleDay(1L, LocalDate.of(2025, 2, 10)))
                .isInstanceOf(ScheduleDaySkippedException.class)
                .hasMessageContaining("No candidates available");

        verify(shiftAssignmentService, never()).assign(any());
    }

    @Test
    void scheduleDay_shouldCallAssignWhenInstancesAndCandidatesExist() {
        Location loc = location(1L);
        Company company = loc.getCompany();
        Position position = Position.builder().id(1L).name("Sales Associate").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        ShiftInstance instance = ShiftInstance.builder()
                .id(10L)
                .location(loc)
                .shiftTemplate(template)
                .date(LocalDate.of(2025, 2, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(1)
                .build();
        Employee employee = Employee.builder().id(100L).name("John").surname("Doe").position(position).shiftsPerWeek(5).build();

        when(locationService.findById(1L)).thenReturn(loc);
        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(1L, LocalDate.of(2025, 2, 10)))
                .thenReturn(List.of(instance));
        when(employeeRepository.findActiveByLocationIdWithShiftPreferences(1L)).thenReturn(List.of(employee));
        when(employeeAvailabilityRepository.findOverlapping(eq(100L), any(), any(), any())).thenReturn(List.of());
        when(shiftAssignmentRepository.countByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(eq(100L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);
        when(shiftAssignmentRepository.findAssignedEmployeeIdsByShiftInstanceIdIn(List.of(10L))).thenReturn(List.of());
        lenient().when(shiftAssignmentRepository.findByShiftInstance_IdAndDeletedAtIsNull(10L)).thenReturn(List.of());
        lenient().when(shiftCandidateTierService.getTier(any(Employee.class), any(ShiftInstance.class), any(LocalDate.class))).thenReturn(1);
        when(shiftAssignmentService.assign(any(ShiftAssignmentRequestDto.class)))
                .thenReturn(new ShiftAssignmentAssignResult(ShiftAssignment.builder().id(1L).build(), List.of()));

        scheduler.scheduleDay(1L, LocalDate.of(2025, 2, 10));

        verify(shiftAssignmentService, atLeastOnce()).assign(any(ShiftAssignmentRequestDto.class));
    }

    @Test
    void scheduleMonth_shouldThrowOnFirstDayWhenNoInstances() {
        Location loc = location(1L);
        when(locationService.findById(1L)).thenReturn(loc);
        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> scheduler.scheduleMonth(1L, java.time.YearMonth.of(2025, 2)))
                .isInstanceOf(ScheduleDaySkippedException.class)
                .hasMessageContaining("No shifts defined");

        verify(shiftInstanceRepository, atLeastOnce()).findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(eq(1L), any(LocalDate.class));
    }

    @Test
    void scheduleDay_shouldThrowWhenAllCandidatesAtShiftsPerWeekCap() {
        Location loc = location(1L);
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        ShiftInstance instance = ShiftInstance.builder()
                .id(10L)
                .location(loc)
                .shiftTemplate(template)
                .date(LocalDate.of(2025, 2, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(1)
                .build();
        Employee employee = Employee.builder().id(100L).name("John").surname("Doe").position(Position.builder().id(1L).company(loc.getCompany()).build()).shiftsPerWeek(5).build();

        when(locationService.findById(1L)).thenReturn(loc);
        when(shiftInstanceRepository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(1L, LocalDate.of(2025, 2, 10)))
                .thenReturn(List.of(instance));
        when(employeeRepository.findActiveByLocationIdWithShiftPreferences(1L)).thenReturn(List.of(employee));
        when(employeeAvailabilityRepository.findOverlapping(eq(100L), any(), any(), any())).thenReturn(List.of());
        when(shiftAssignmentRepository.countByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(eq(100L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(5L);

        assertThatThrownBy(() -> scheduler.scheduleDay(1L, LocalDate.of(2025, 2, 10)))
                .isInstanceOf(ScheduleDaySkippedException.class)
                .hasMessageContaining("No candidates under 5 shifts/week");

        verify(shiftAssignmentService, never()).assign(any());
    }
}
