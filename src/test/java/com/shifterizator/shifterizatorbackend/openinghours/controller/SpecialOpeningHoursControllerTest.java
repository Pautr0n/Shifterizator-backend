package com.shifterizator.shifterizatorbackend.openinghours.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursNotFoundException;
import com.shifterizator.shifterizatorbackend.openinghours.exception.SpecialOpeningHoursValidationException;
import com.shifterizator.shifterizatorbackend.openinghours.mapper.SpecialOpeningHoursMapper;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.service.SpecialOpeningHoursService;
import com.shifterizator.shifterizatorbackend.company.model.Location;
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
@WebMvcTest(SpecialOpeningHoursController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpecialOpeningHoursControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpecialOpeningHoursService openingHoursService;

    @MockitoBean
    private SpecialOpeningHoursMapper openingHoursMapper;

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

    private SpecialOpeningHours openingHours;
    private SpecialOpeningHoursResponseDto responseDto;
    private SpecialOpeningHoursRequestDto requestDto;
    private static final LocalDate DATE = LocalDate.of(2024, 12, 24);

    private String toJson(long locationId, String date, String openTime, String closeTime,
                          String reason, String colorCode, boolean appliesToCompany) {
        return String.format("{\"locationId\":%d,\"date\":\"%s\",\"openTime\":\"%s\",\"closeTime\":\"%s\",\"reason\":\"%s\",\"colorCode\":\"%s\",\"appliesToCompany\":%b}",
                locationId, date, openTime, closeTime, reason, colorCode, appliesToCompany);
    }

    @BeforeEach
    void setUp() {
        Location location = Location.builder().id(10L).name("HQ").address("Main").build();
        openingHours = SpecialOpeningHours.builder()
                .id(99L)
                .location(location)
                .date(DATE)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas Eve")
                .colorCode("#FF0000")
                .appliesToCompany(false)
                .build();

        responseDto = new SpecialOpeningHoursResponseDto(
                99L,
                10L,
                "HQ",
                DATE,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Christmas Eve",
                "#FF0000",
                false,
                null,
                null,
                null,
                null
        );

        requestDto = new SpecialOpeningHoursRequestDto(
                10L,
                DATE,
                LocalTime.of(9, 0),
                LocalTime.of(18, 0),
                "Christmas Eve",
                "#FF0000",
                false
        );
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(openingHoursService.create(any())).thenReturn(openingHours);
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(post("/api/opening-hours")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "09:00:00", "18:00:00", "Christmas Eve", "#FF0000", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10))
                .andExpect(jsonPath("$.reason").value("Christmas Eve"));

        verify(openingHoursService).create(any());
    }

    @Test
    void create_shouldReturn400WhenValidationError() throws Exception {
        when(openingHoursService.create(any()))
                .thenThrow(new SpecialOpeningHoursValidationException("Close time must be after open time"));

        mockMvc.perform(post("/api/opening-hours")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "18:00:00", "09:00:00", "Bad", "#FF0000", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(openingHoursService).create(any());
    }

    @Test
    void update_shouldReturn200AndBody() throws Exception {
        when(openingHoursService.update(eq(99L), any())).thenReturn(openingHours);
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(put("/api/opening-hours/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "09:00:00", "18:00:00", "Christmas Eve", "#FF0000", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(openingHoursService).update(eq(99L), any());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(openingHoursService.update(eq(999L), any()))
                .thenThrow(new SpecialOpeningHoursNotFoundException("Special opening hours not found"));

        mockMvc.perform(put("/api/opening-hours/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "09:00:00", "18:00:00", "Christmas Eve", "#FF0000", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(openingHoursService).update(eq(999L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/opening-hours/99").with(csrf()))
                .andExpect(status().isNoContent());

        verify(openingHoursService).delete(99L, false);
    }

    @Test
    void delete_shouldPassHardDeleteParam() throws Exception {
        mockMvc.perform(delete("/api/opening-hours/99").with(csrf()).param("hardDelete", "true"))
                .andExpect(status().isNoContent());

        verify(openingHoursService).delete(99L, true);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(openingHoursService.findById(99L)).thenReturn(openingHours);
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(get("/api/opening-hours/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10));

        verify(openingHoursService).findById(99L);
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(openingHoursService.findById(999L))
                .thenThrow(new SpecialOpeningHoursNotFoundException("Special opening hours not found"));

        mockMvc.perform(get("/api/opening-hours/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Special opening hours not found"));

        verify(openingHoursService).findById(999L);
    }

    @Test
    void search_shouldReturnPagedResults() throws Exception {
        when(openingHoursService.search(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(openingHours)));
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(get("/api/opening-hours").with(csrf()).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(openingHoursService).search(any(), any(), any());
    }

    @Test
    void findByLocation_shouldReturnList() throws Exception {
        when(openingHoursService.findByLocation(10L)).thenReturn(List.of(openingHours));
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(get("/api/opening-hours/by-location/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(openingHoursService).findByLocation(10L);
    }

    @Test
    void findByLocationAndMonth_shouldReturnList() throws Exception {
        when(openingHoursService.findByLocationAndMonth(10L, YearMonth.of(2024, 12)))
                .thenReturn(List.of(openingHours));
        when(openingHoursMapper.toDto(openingHours)).thenReturn(responseDto);

        mockMvc.perform(get("/api/opening-hours/month")
                        .with(csrf())
                        .param("locationId", "10")
                        .param("month", "2024-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(openingHoursService).findByLocationAndMonth(10L, YearMonth.of(2024, 12));
    }
}

