package com.shifterizator.shifterizatorbackend.availability.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityNotFoundException;
import com.shifterizator.shifterizatorbackend.availability.exception.AvailabilityValidationException;
import com.shifterizator.shifterizatorbackend.availability.exception.OverlappingAvailabilityException;
import com.shifterizator.shifterizatorbackend.availability.mapper.AvailabilityMapper;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.service.AvailabilityService;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(AvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    @MockitoBean
    private AvailabilityMapper availabilityMapper;

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

    private String toJson(long employeeId, String startDate, String endDate, String type) {
        return String.format("{\"employeeId\":%d,\"startDate\":\"%s\",\"endDate\":\"%s\",\"type\":\"%s\"}",
                employeeId, startDate, endDate, type);
    }

    private EmployeeAvailability availability;
    private AvailabilityResponseDto responseDto;
    private AvailabilityRequestDto requestDto;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate NEXT_WEEK = LocalDate.now().plusDays(7);

    @BeforeEach
    void setUp() {
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").build();
        availability = EmployeeAvailability.builder()
                .id(99L)
                .employee(employee)
                .startDate(TOMORROW)
                .endDate(NEXT_WEEK)
                .type(AvailabilityType.VACATION)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        responseDto = new AvailabilityResponseDto(
                99L, 1L, "John Doe", TOMORROW, NEXT_WEEK, AvailabilityType.VACATION,
                availability.getCreatedAt(), availability.getUpdatedAt(), null, null
        );
        requestDto = new AvailabilityRequestDto(1L, TOMORROW, NEXT_WEEK, AvailabilityType.VACATION);
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(availabilityService.create(any())).thenReturn(availability);
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(post("/api/availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, TOMORROW.toString(), NEXT_WEEK.toString(), "VACATION")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.type").value("VACATION"));

        verify(availabilityService).create(any());
    }

    @Test
    void create_shouldReturn400WhenValidationError() throws Exception {
        when(availabilityService.create(any())).thenThrow(new AvailabilityValidationException("End date must be on or after start date"));

        mockMvc.perform(post("/api/availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, TOMORROW.toString(), TOMORROW.minusDays(1).toString(), "VACATION")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(availabilityService).create(any());
    }

    @Test
    void create_shouldReturn400WhenOverlapping() throws Exception {
        when(availabilityService.create(any())).thenThrow(new OverlappingAvailabilityException("Another availability record overlaps"));

        mockMvc.perform(post("/api/availability")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, TOMORROW.toString(), NEXT_WEEK.toString(), "VACATION")))
                .andExpect(status().isBadRequest());

        verify(availabilityService).create(any());
    }

    @Test
    void update_shouldReturn200AndBody() throws Exception {
        when(availabilityService.update(eq(99L), any())).thenReturn(availability);
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(put("/api/availability/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, TOMORROW.toString(), NEXT_WEEK.toString(), "VACATION")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.type").value("VACATION"));

        verify(availabilityService).update(eq(99L), any());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(availabilityService.update(eq(999L), any())).thenThrow(new AvailabilityNotFoundException("Availability not found"));

        mockMvc.perform(put("/api/availability/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, TOMORROW.toString(), NEXT_WEEK.toString(), "VACATION")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(availabilityService).update(eq(999L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/availability/99").with(csrf()))
                .andExpect(status().isNoContent());

        verify(availabilityService).delete(99L, false);
    }

    @Test
    void delete_shouldPassHardDeleteParam() throws Exception {
        mockMvc.perform(delete("/api/availability/99").with(csrf()).param("hardDelete", "true"))
                .andExpect(status().isNoContent());

        verify(availabilityService).delete(99L, true);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new AvailabilityNotFoundException("Availability not found")).when(availabilityService).delete(999L, false);

        mockMvc.perform(delete("/api/availability/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(availabilityService).delete(999L, false);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(availabilityService.findById(99L)).thenReturn(availability);
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(get("/api/availability/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.type").value("VACATION"));

        verify(availabilityService).findById(99L);
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(availabilityService.findById(999L)).thenThrow(new AvailabilityNotFoundException("Availability not found"));

        mockMvc.perform(get("/api/availability/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Availability not found"));

        verify(availabilityService).findById(999L);
    }

    @Test
    void search_shouldReturn200AndPage() throws Exception {
        when(availabilityService.search(any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(availability)));
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(get("/api/availability").with(csrf()).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(availabilityService).search(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findByEmployee_shouldReturn200AndList() throws Exception {
        when(availabilityService.findByEmployee(1L)).thenReturn(List.of(availability));
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(get("/api/availability/by-employee/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(availabilityService).findByEmployee(1L);
    }

    @Test
    void findByRange_shouldReturn200AndList() throws Exception {
        when(availabilityService.findByRange(TOMORROW, NEXT_WEEK)).thenReturn(List.of(availability));
        when(availabilityMapper.toDto(availability)).thenReturn(responseDto);

        mockMvc.perform(get("/api/availability/range")
                        .with(csrf())
                        .param("start", TOMORROW.toString())
                        .param("end", NEXT_WEEK.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(availabilityService).findByRange(TOMORROW, NEXT_WEEK);
    }
}
