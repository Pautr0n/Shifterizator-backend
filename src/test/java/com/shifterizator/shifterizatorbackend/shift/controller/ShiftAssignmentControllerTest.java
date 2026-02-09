package com.shifterizator.shifterizatorbackend.shift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentAssignResult;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentResponseDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftAssignmentNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftAssignmentMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(ShiftAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftAssignmentService shiftAssignmentService;

    @MockitoBean
    private ShiftAssignmentMapper shiftAssignmentMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class JacksonConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private ShiftAssignment assignment;
    private ShiftAssignmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(10L).name("HQ").address("Main").company(company).build();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();
        ShiftInstance instance = ShiftInstance.builder().id(99L).shiftTemplate(template).location(location).build();

        assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(instance)
                .employee(employee)
                .isConfirmed(false)
                .assignedAt(LocalDateTime.now())
                .build();

        responseDto = new ShiftAssignmentResponseDto(
                100L,
                99L,
                1L,
                "John Doe",
                false,
                LocalDateTime.now(),
                null,
                null,
                List.of()
        );
    }

    @Test
    void assign_shouldReturn201AndBody() throws Exception {
        when(shiftAssignmentService.assign(any())).thenReturn(new ShiftAssignmentAssignResult(assignment, List.of()));
        when(shiftAssignmentMapper.toDto(assignment, List.of())).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shiftInstanceId\":99,\"employeeId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.employeeId").value(1));

        verify(shiftAssignmentService).assign(any());
    }

    @Test
    void assign_shouldIncludeWarningsInResponse() throws Exception {
        var warnings = List.of("Assignment is on employee's preferred day off (MONDAY).");
        var responseWithWarnings = new ShiftAssignmentResponseDto(
                100L, 99L, 1L, "John Doe", false, LocalDateTime.now(), null, null, warnings);
        when(shiftAssignmentService.assign(any())).thenReturn(new ShiftAssignmentAssignResult(assignment, warnings));
        when(shiftAssignmentMapper.toDto(assignment, warnings)).thenReturn(responseWithWarnings);

        mockMvc.perform(post("/api/shift-assignments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shiftInstanceId\":99,\"employeeId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings.length()").value(1))
                .andExpect(jsonPath("$.warnings[0]").value("Assignment is on employee's preferred day off (MONDAY)."));

        verify(shiftAssignmentService).assign(any());
    }

    @Test
    void unassign_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/shift-assignments/shift-instance/99/employee/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(shiftAssignmentService).unassign(99L, 1L);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(shiftAssignmentService.findById(100L)).thenReturn(assignment);
        when(shiftAssignmentMapper.toDto(assignment)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-assignments/100").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));

        verify(shiftAssignmentService).findById(100L);
    }

    @Test
    void findByShiftInstance_shouldReturnList() throws Exception {
        when(shiftAssignmentService.findByShiftInstance(99L)).thenReturn(List.of(assignment));
        when(shiftAssignmentMapper.toDto(assignment)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-assignments/shift-instance/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));

        verify(shiftAssignmentService).findByShiftInstance(99L);
    }

    @Test
    void findByEmployee_shouldReturnList() throws Exception {
        when(shiftAssignmentService.findByEmployee(1L)).thenReturn(List.of(assignment));
        when(shiftAssignmentMapper.toDto(assignment)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-assignments/employee/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));

        verify(shiftAssignmentService).findByEmployee(1L);
    }
}
