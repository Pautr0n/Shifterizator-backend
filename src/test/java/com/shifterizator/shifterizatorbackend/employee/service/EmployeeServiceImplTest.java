package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.exception.PositionNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.employee.service.domain.EmployeeDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private EmployeeDomainService employeeDomainService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void create_shouldCreateEmployeeSuccessfully() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                1L, Set.of(1L), Set.of(10L), Set.of(1L), null, null, null
        );

        Position position = Position.builder().id(1L).name("Waiter").build();
        Employee employee = Employee.builder()
                .id(99L)
                .name("John")
                .surname("Connor")
                .email("john@example.com")
                .phone("123")
                .position(position)
                .build();

        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));
        when(employeeMapper.toEntity(dto, position)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.create(dto);

        verify(employeeDomainService).validateEmailUniqueness(dto, null);
        verify(employeeMapper).toEntity(dto, position);
        verify(employeeDomainService).assignUser(any(Employee.class), eq(dto), eq(null));
        verify(employeeRepository).save(any(Employee.class));
        verify(employeeDomainService).assignCompanies(any(Employee.class), eq(dto));
        verify(employeeDomainService).assignLocations(any(Employee.class), eq(dto));
        verify(employeeDomainService).assignLanguages(any(Employee.class), eq(dto));

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getPosition().getName()).isEqualTo("Waiter");
    }

    @Test
    void create_shouldThrowWhenPositionNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                1L, Set.of(1L), Set.of(10L), Set.of(1L), null, null, null
        );

        when(positionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(dto))
                .isInstanceOf(PositionNotFoundException.class)
                .hasMessage("Position not found");
    }

    @Test
    void update_shouldUpdateEmployeeSuccessfully() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                1L, Set.of(1L), Set.of(10L), Set.of(1L), null, null, null
        );

        Position position = Position.builder().id(1L).name("Waiter").build();
        Employee employee = Employee.builder()
                .id(99L)
                .name("Old")
                .surname("Name")
                .email("old@example.com")
                .phone("000")
                .position(position)
                .build();

        when(employeeRepository.findActiveById(99L)).thenReturn(Optional.of(employee));
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        Employee result = employeeService.update(99L, dto);

        verify(employeeDomainService).validateEmailUniqueness(dto, 99L);
        verify(employeeDomainService).assignUser(employee, dto, 99L);
        verify(employeeDomainService).assignCompanies(employee, dto);
        verify(employeeDomainService).assignLocations(employee, dto);
        verify(employeeDomainService).assignLanguages(employee, dto);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPosition().getName()).isEqualTo("Waiter");
    }

    @Test
    void update_shouldThrowWhenEmployeeNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                1L, Set.of(1L), Set.of(10L), Set.of(1L), null, null, null
        );

        when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.update(99L, dto))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void delete_shouldHardDeleteWhenFlagTrue() {
        Employee employee = Employee.builder().id(99L).build();

        when(employeeRepository.findById(99L)).thenReturn(Optional.of(employee));

        employeeService.delete(99L, true);

        verify(employeeDomainService).ensureEmployeeCanBeDeleted(99L);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void delete_shouldSoftDeleteWhenFlagFalse() {
        Employee employee = Employee.builder().id(99L).build();

        when(employeeRepository.findById(99L)).thenReturn(Optional.of(employee));

        employeeService.delete(99L, false);

        verify(employeeDomainService).ensureEmployeeCanBeDeleted(99L);
        assertThat(employee.getDeletedAt()).isNotNull();
        verify(employeeRepository, never()).delete(any(Employee.class));
    }

    @Test
    void delete_shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(99L, true))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void findById_shouldReturnEmployee() {
        Employee employee = Employee.builder()
                .id(99L)
                .name("John")
                .build();

        when(employeeRepository.findActiveById(99L)).thenReturn(Optional.of(employee));

        Employee result = employeeService.findById(99L);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(employeeRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("Employee not found");
    }

    @Test
    void search_shouldDelegateToRepository() {
        Employee employee = Employee.builder().id(99L).build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.search(
                1L, 10L, "john", "Waiter", pageable
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(99L);
        verify(employeeRepository).findAll(any(Specification.class), eq(pageable));
    }

}
