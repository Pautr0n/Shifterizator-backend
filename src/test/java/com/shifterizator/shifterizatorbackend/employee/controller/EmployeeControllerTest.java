package com.shifterizator.shifterizatorbackend.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.exception.EmployeeNotFoundException;
import com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.service.EmployeeService;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private EmployeeMapper employeeMapper;

    @Autowired
    private ObjectMapper mapper;

    @TestConfiguration
    static class JacksonConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private EmployeeRequestDto requestDto;
    private EmployeeResponseDto responseDto;
    private Employee employee;


    @BeforeEach
    void setUp() {
        requestDto = new EmployeeRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "123456789",
                1L,
                Set.of(10L),
                Set.of(20L),
                Set.of(1L),
                null
        );

        Position position = Position.builder().id(1L).name("Waiter").build();
        employee = Employee.builder()
                .id(99L)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .phone("123456789")
                .position(position)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 12, 0))
                .build();

        responseDto = new EmployeeResponseDto(
                99L,
                "John",
                "Doe",
                "john@example.com",
                "123456789",
                "Manager",
                Set.of("Company A", "Company B"),
                Set.of("Barcelona", "Madrid"),
                Set.of("English", "Spanish"),
                null,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 12, 0)
        );
    }

    @Test
    void create_should_return_201() throws Exception {
        when(employeeService.create(any())).thenReturn(employee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(responseDto);

        mvc.perform(post("/api/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.phone").value("123456789"))
                .andExpect(jsonPath("$.position").value("Manager"))
                .andExpect(jsonPath("$.companies").isArray())
                .andExpect(jsonPath("$.companies").value(org.hamcrest.Matchers.hasItems("Company A", "Company B")))
                .andExpect(jsonPath("$.locations").isArray())
                .andExpect(jsonPath("$.locations").value(org.hamcrest.Matchers.hasItems("Barcelona", "Madrid")))
                .andExpect(jsonPath("$.languages").value(org.hamcrest.Matchers.hasItems("English", "Spanish")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(employeeService).create(any());
        verify(employeeMapper).toResponse(employee);
    }


    @Test
    void create_shouldReturn400_whenInvalidDto() throws Exception {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "", "", "invalid", "123",
                null, Set.of(), null, null, null
        );

        mvc.perform(post("/api/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).create(any());
    }

    @Test
    void createEmployee_should_return_400_when_validation_error() throws Exception {
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                1L, Set.of(1L), Set.of(10L), null, null
        );

        when(employeeService.create(any()))
                .thenThrow(new EmailAlreadyExistsException("Email already exists for this company"));

        mvc.perform(post("/api/employees")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists for this company"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(employeeService).create(any());
    }


    @Test
    void findById_shouldReturn200WhenOk() throws Exception {
        when(employeeService.findById(99L)).thenReturn(employee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(responseDto);

        mvc.perform(get("/api/employees/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(employeeService).findById(99L);
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        when(employeeService.findById(99L))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(employeeService).findById(99L);
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/api/employees/99")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("hardDelete", "true"))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(99L, true);
    }

    @Test
    void delete_shouldReturn204_withHardDeleteFalse() throws Exception {
        mvc.perform(delete("/api/employees/99")
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService).delete(99L, false);
    }

    @Test
    void update_should_return_200_when_success() throws Exception {
        when(employeeService.update(eq(99L), any())).thenReturn(employee);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(responseDto);

        mvc.perform(put("/api/employees/99")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.name").value("John"));

        verify(employeeService).update(eq(99L), any());
        verify(employeeMapper).toResponse(employee);
    }

    @Test
    void update_should_return_404_when_employee_not_found() throws Exception {
        when(employeeService.update(eq(99L), any()))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mvc.perform(put("/api/employees/99")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(employeeService).update(eq(99L), any());
    }

    @Test
    void update_should_return_400_when_validation_error() throws Exception {
        when(employeeService.update(eq(99L), any()))
                .thenThrow(new EmailAlreadyExistsException("Email already exists for this company"));

        mvc.perform(put("/api/employees/99")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists for this company"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(employeeService).update(eq(99L), any());
    }

    @Test
    void search_shouldReturnPagedResults() throws Exception {
        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(employeeService.search(any(), any(), any(), any(), any())).thenReturn(page);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(responseDto);

        mvc.perform(get("/api/employees")
                        .param("name", "john")
                        .param("position", "Waiter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(employeeService).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_should_return_empty_page_when_no_results() throws Exception {
        Page<Employee> emptyPage = new PageImpl<>(Collections.emptyList());

        when(employeeService.search(any(), any(), any(), any(), any())).thenReturn(emptyPage);

        mvc.perform(get("/api/employees")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(employeeService).search(any(), any(), any(), any(), any());
    }

    @Test
    void search_should_return_200_with_all_params() throws Exception {
        Page<Employee> page = new PageImpl<>(List.of(employee));

        when(employeeService.search(eq(1L), eq(10L), eq("john"), eq("Waiter"), any())).thenReturn(page);
        when(employeeMapper.toResponse(any(Employee.class))).thenReturn(responseDto);

        mvc.perform(get("/api/employees")
                        .param("companyId", "1")
                        .param("locationId", "10")
                        .param("name", "john")
                        .param("position", "Waiter")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(employeeService).search(eq(1L), eq(10L), eq("john"), eq("Waiter"), any());
    }

}