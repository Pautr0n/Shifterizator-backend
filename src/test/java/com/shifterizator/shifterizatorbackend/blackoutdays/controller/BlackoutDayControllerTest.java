package com.shifterizator.shifterizatorbackend.blackoutdays.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayNotFoundException;
import com.shifterizator.shifterizatorbackend.blackoutdays.exception.BlackoutDayValidationException;
import com.shifterizator.shifterizatorbackend.blackoutdays.mapper.BlackoutDayMapper;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.service.BlackoutDayService;
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
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(BlackoutDayController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlackoutDayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlackoutDayService blackoutDayService;

    @MockitoBean
    private BlackoutDayMapper blackoutDayMapper;

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

    private BlackoutDay blackoutDay;
    private BlackoutDayResponseDto responseDto;
    private BlackoutDayRequestDto requestDto;
    private static final LocalDate DATE = LocalDate.of(2024, 12, 24);

    private String toJson(long locationId, String date, String reason, boolean appliesToCompany) {
        return String.format("{\"locationId\":%d,\"date\":\"%s\",\"reason\":\"%s\",\"appliesToCompany\":%b}",
                locationId, date, reason, appliesToCompany);
    }

    @BeforeEach
    void setUp() {
        Location location = Location.builder().id(10L).name("HQ").address("Main").build();
        blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(DATE)
                .reason("Holiday closure")
                .appliesToCompany(false)
                .build();

        responseDto = new BlackoutDayResponseDto(
                99L,
                10L,
                "HQ",
                DATE,
                "Holiday closure",
                false,
                null,
                null,
                null,
                null
        );

        requestDto = new BlackoutDayRequestDto(
                10L,
                DATE,
                "Holiday closure",
                false
        );
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(blackoutDayService.create(any())).thenReturn(blackoutDay);
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(post("/api/blackout-days")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "Holiday closure", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10))
                .andExpect(jsonPath("$.reason").value("Holiday closure"));

        verify(blackoutDayService).create(any());
    }

    @Test
    void create_shouldReturn400WhenValidationError() throws Exception {
        when(blackoutDayService.create(any()))
                .thenThrow(new BlackoutDayValidationException("A special opening hours record already exists"));

        mockMvc.perform(post("/api/blackout-days")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "Holiday closure", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(blackoutDayService).create(any());
    }

    @Test
    void update_shouldReturn200AndBody() throws Exception {
        when(blackoutDayService.update(eq(99L), any())).thenReturn(blackoutDay);
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(put("/api/blackout-days/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "Holiday closure", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(blackoutDayService).update(eq(99L), any());
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(blackoutDayService.update(eq(999L), any()))
                .thenThrow(new BlackoutDayNotFoundException("Blackout day not found"));

        mockMvc.perform(put("/api/blackout-days/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(10L, DATE.toString(), "Holiday closure", false)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(blackoutDayService).update(eq(999L), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/blackout-days/99").with(csrf()))
                .andExpect(status().isNoContent());

        verify(blackoutDayService).delete(99L, false);
    }

    @Test
    void delete_shouldPassHardDeleteParam() throws Exception {
        mockMvc.perform(delete("/api/blackout-days/99").with(csrf()).param("hardDelete", "true"))
                .andExpect(status().isNoContent());

        verify(blackoutDayService).delete(99L, true);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(blackoutDayService.findById(99L)).thenReturn(blackoutDay);
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(get("/api/blackout-days/99").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.locationId").value(10));

        verify(blackoutDayService).findById(99L);
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(blackoutDayService.findById(999L))
                .thenThrow(new BlackoutDayNotFoundException("Blackout day not found"));

        mockMvc.perform(get("/api/blackout-days/999").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Blackout day not found"));

        verify(blackoutDayService).findById(999L);
    }

    @Test
    void search_shouldReturnPagedResults() throws Exception {
        when(blackoutDayService.search(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(blackoutDay)));
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(get("/api/blackout-days").with(csrf()).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99));

        verify(blackoutDayService).search(any(), any(), any());
    }

    @Test
    void findByLocation_shouldReturnList() throws Exception {
        when(blackoutDayService.findByLocation(10L)).thenReturn(List.of(blackoutDay));
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(get("/api/blackout-days/by-location/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(blackoutDayService).findByLocation(10L);
    }

    @Test
    void findByLocationAndMonth_shouldReturnList() throws Exception {
        when(blackoutDayService.findByLocationAndMonth(10L, YearMonth.of(2024, 12)))
                .thenReturn(List.of(blackoutDay));
        when(blackoutDayMapper.toDto(blackoutDay)).thenReturn(responseDto);

        mockMvc.perform(get("/api/blackout-days/month")
                        .with(csrf())
                        .param("locationId", "10")
                        .param("month", "2024-12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99));

        verify(blackoutDayService).findByLocationAndMonth(10L, YearMonth.of(2024, 12));
    }
}
