package com.shifterizator.shifterizatorbackend.shift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftGenerationService;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftInstanceNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftInstanceMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftInstanceService;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftSchedulerService;
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
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(ShiftInstanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftInstanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftInstanceService shiftInstanceService;

    @MockitoBean
    private ShiftInstanceMapper shiftInstanceMapper;

    @MockitoBean
    private ShiftInstanceRepository shiftInstanceRepository;

    @MockitoBean
    private ShiftGenerationService shiftGenerationService;

    @MockitoBean
    private ShiftSchedulerService shiftSchedulerService;

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

    private ShiftInstance instance;
    private ShiftInstanceResponseDto responseDto;
    private static final LocalDate DATE = LocalDate.of(2024, 12, 24);
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);

    private String toJson(long shiftTemplateId, long locationId, String date, String startTime, String endTime, int requiredEmployees) {
        return String.format(
                "{\"shiftTemplateId\":%d,\"locationId\":%d,\"date\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"requiredEmployees\":%d,\"notes\":null}",
                shiftTemplateId, locationId, date, startTime, endTime, requiredEmployees);
    }

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(10L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        instance = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(DATE)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .requiredEmployees(3)
                .isComplete(false)
                .build();

        responseDto = new ShiftInstanceResponseDto(
                99L,
                1L,
                10L,
                "HQ",
                DATE,
                START_TIME,
                END_TIME,
                3,
                2,
                false,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(shiftInstanceService.create(any())).thenReturn(instance);
        when(shiftInstanceRepository.countActiveAssignments(99L)).thenReturn(0);
        when(shiftInstanceMapper.toDto(instance, 0)).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-instances")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(1L, 10L, DATE.toString(), "09:00:00", "17:00:00", 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10));

        verify(shiftInstanceService).create(any());
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(shiftInstanceService.findById(99L)).thenReturn(instance);
        when(shiftInstanceRepository.countActiveAssignments(99L)).thenReturn(2);
        when(shiftInstanceMapper.toDto(instance, 2)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-instances/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.assignedEmployees").value(2));

        verify(shiftInstanceService).findById(99L);
    }

    @Test
    void findByLocationAndDate_shouldReturnList() throws Exception {
        when(shiftInstanceService.findByLocationAndDate(10L, DATE)).thenReturn(List.of(instance));
        when(shiftInstanceRepository.countActiveAssignments(99L)).thenReturn(2);
        when(shiftInstanceMapper.toDto(instance, 2)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-instances/by-location/10/date/" + DATE.toString()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(shiftInstanceService).findByLocationAndDate(10L, DATE);
    }

    @Test
    void generateMonth_shouldReturn201AndListOfInstances() throws Exception {
        when(shiftGenerationService.generateMonth(10L, YearMonth.of(2025, 2))).thenReturn(List.of(instance));
        when(shiftInstanceRepository.countActiveAssignments(99L)).thenReturn(0);
        when(shiftInstanceMapper.toDto(instance, 0)).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":10,\"year\":2025,\"month\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(99))
                .andExpect(jsonPath("$[0].locationId").value(10));

        verify(shiftGenerationService).generateMonth(10L, YearMonth.of(2025, 2));
    }

    @Test
    void scheduleDay_shouldReturn202Accepted() throws Exception {
        mockMvc.perform(post("/api/shift-instances/schedule-day")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":10,\"date\":\"2025-02-10\"}"))
                .andExpect(status().isAccepted());

        verify(shiftSchedulerService).scheduleDay(10L, LocalDate.of(2025, 2, 10));
    }

    @Test
    void scheduleMonth_shouldReturn202Accepted() throws Exception {
        mockMvc.perform(post("/api/shift-instances/schedule-month")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":10,\"year\":2025,\"month\":2}"))
                .andExpect(status().isAccepted());

        verify(shiftSchedulerService).scheduleMonth(10L, YearMonth.of(2025, 2));
    }
}
