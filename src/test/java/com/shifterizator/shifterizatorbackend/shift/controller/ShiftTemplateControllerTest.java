package com.shifterizator.shifterizatorbackend.shift.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftTemplateNotFoundException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import com.shifterizator.shifterizatorbackend.shift.mapper.ShiftTemplateMapper;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.service.ShiftTemplateService;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(ShiftTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftTemplateService shiftTemplateService;

    @MockitoBean
    private ShiftTemplateMapper shiftTemplateMapper;

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

    private ShiftTemplate template;
    private ShiftTemplateResponseDto responseDto;
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);

    private String toJson(long locationId, String startTime, String endTime, String description, boolean isActive) {
        return String.format(
                "{\"locationId\":%d,\"requiredPositions\":[{\"positionId\":1,\"requiredCount\":2},{\"positionId\":2,\"requiredCount\":1}],\"startTime\":\"%s\",\"endTime\":\"%s\",\"description\":\"%s\",\"requiredLanguageIds\":[],\"requiredLanguageRequirements\":null,\"isActive\":%b}",
                locationId, startTime, endTime, description, isActive);
    }

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(10L).name("HQ").address("Main").company(company).build();

        template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .description("Morning shift")
                .isActive(true)
                .build();

        responseDto = new ShiftTemplateResponseDto(
                99L,
                10L,
                "HQ",
                List.of(
                        new PositionRequirementResponseDto(1L, "Sales Assistant", 2, null),
                        new PositionRequirementResponseDto(2L, "Manager", 1, null)
                ),
                START_TIME,
                END_TIME,
                3,
                null,
                "Morning shift",
                Set.of(),
                List.of(),
                true,
                1,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(shiftTemplateService.create(any())).thenReturn(template);
        when(shiftTemplateMapper.toDto(template)).thenReturn(responseDto);

        mockMvc.perform(post("/api/shift-templates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, "09:00:00", "17:00:00", "Morning shift", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10))
                .andExpect(jsonPath("$.totalRequiredEmployees").value(3));

        verify(shiftTemplateService).create(any());
    }

    @Test
    void update_shouldReturn200AndBody() throws Exception {
        when(shiftTemplateService.update(eq(99L), any())).thenReturn(template);
        when(shiftTemplateMapper.toDto(template)).thenReturn(responseDto);

        mockMvc.perform(put("/api/shift-templates/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, "09:00:00", "17:00:00", "Morning shift", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(shiftTemplateService).update(eq(99L), any());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(shiftTemplateService.update(eq(999L), any()))
                .thenThrow(new ShiftTemplateNotFoundException("Shift template not found"));

        mockMvc.perform(put("/api/shift-templates/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, "09:00:00", "17:00:00", "Morning shift", true)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(shiftTemplateService).update(eq(999L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/shift-templates/99").with(csrf()))
                .andExpect(status().isNoContent());

        verify(shiftTemplateService).delete(99L, false);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(shiftTemplateService.findById(99L)).thenReturn(template);
        when(shiftTemplateMapper.toDto(template)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-templates/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10));

        verify(shiftTemplateService).findById(99L);
    }

    @Test
    void search_shouldReturnPagedResults() throws Exception {
        when(shiftTemplateService.search(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(template)));
        when(shiftTemplateMapper.toDto(template)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-templates").with(csrf()).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(shiftTemplateService).search(any(), any(), any());
    }

    @Test
    void findByLocation_shouldReturnList() throws Exception {
        when(shiftTemplateService.findByLocation(10L)).thenReturn(List.of(template));
        when(shiftTemplateMapper.toDto(template)).thenReturn(responseDto);

        mockMvc.perform(get("/api/shift-templates/by-location/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(shiftTemplateService).findByLocation(10L);
    }
}
