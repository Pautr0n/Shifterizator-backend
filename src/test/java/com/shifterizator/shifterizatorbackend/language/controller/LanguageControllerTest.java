package com.shifterizator.shifterizatorbackend.language.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageRequestDto;
import com.shifterizator.shifterizatorbackend.language.dto.LanguageResponseDto;
import com.shifterizator.shifterizatorbackend.language.exception.LanguageNotFoundException;
import com.shifterizator.shifterizatorbackend.language.mapper.LanguageMapper;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.service.LanguageService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@WebMvcTest(LanguageController.class)
@AutoConfigureMockMvc(addFilters = false)
class LanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LanguageService languageService;

    @MockitoBean
    private LanguageMapper languageMapper;

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

    private Language language;
    private LanguageResponseDto responseDto;
    private LanguageRequestDto requestDto;

    @BeforeEach
    void setUp() {
        language = Language.builder()
                .id(1L)
                .code("EN")
                .name("English")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 12, 0))
                .build();

        responseDto = new LanguageResponseDto(
                1L,
                "EN",
                "English",
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 2, 12, 0)
        );

        requestDto = new LanguageRequestDto("EN", "English");
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        when(languageService.create(any())).thenReturn(language);
        when(languageMapper.toDto(language)).thenReturn(responseDto);

        mockMvc.perform(post("/api/languages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("EN"))
                .andExpect(jsonPath("$.name").value("English"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(languageService).create(any());
        verify(languageMapper).toDto(language);
    }

    @Test
    void create_shouldReturn400WhenValidationError() throws Exception {
        LanguageRequestDto invalidDto = new LanguageRequestDto("", "");

        mockMvc.perform(post("/api/languages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).create(any());
    }

    @Test
    void update_shouldReturn200AndBody() throws Exception {
        when(languageService.update(eq(1L), any())).thenReturn(language);
        when(languageMapper.toDto(language)).thenReturn(responseDto);

        mockMvc.perform(put("/api/languages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("EN"))
                .andExpect(jsonPath("$.name").value("English"));

        verify(languageService).update(eq(1L), any());
        verify(languageMapper).toDto(language);
    }

    @Test
    void update_shouldReturn404WhenLanguageNotFound() throws Exception {
        when(languageService.update(eq(99L), any()))
                .thenThrow(new LanguageNotFoundException("Language not found"));

        mockMvc.perform(put("/api/languages/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Language not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(languageService).update(eq(99L), any());
    }

    @Test
    void update_shouldReturn400WhenValidationError() throws Exception {
        LanguageRequestDto invalidDto = new LanguageRequestDto("E", "X");

        mockMvc.perform(put("/api/languages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(languageService, never()).update(any(), any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/languages/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(languageService).delete(1L);
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new LanguageNotFoundException("Language not found"))
                .when(languageService).delete(99L);

        mockMvc.perform(delete("/api/languages/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Language not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(languageService).delete(99L);
    }

    @Test
    void findById_shouldReturn200AndBody() throws Exception {
        when(languageService.findById(1L)).thenReturn(language);
        when(languageMapper.toDto(language)).thenReturn(responseDto);

        mockMvc.perform(get("/api/languages/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("EN"))
                .andExpect(jsonPath("$.name").value("English"));

        verify(languageService).findById(1L);
        verify(languageMapper).toDto(language);
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(languageService.findById(99L))
                .thenThrow(new LanguageNotFoundException("Language not found"));

        mockMvc.perform(get("/api/languages/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Language not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(languageService).findById(99L);
    }

    @Test
    void findAll_shouldReturn200AndList() throws Exception {
        when(languageService.findAll()).thenReturn(List.of(language));
        when(languageMapper.toDto(language)).thenReturn(responseDto);

        mockMvc.perform(get("/api/languages").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("EN"))
                .andExpect(jsonPath("$[0].name").value("English"));

        verify(languageService).findAll();
    }

    @Test
    void findAll_shouldReturn200AndEmptyList() throws Exception {
        when(languageService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/languages").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(languageService).findAll();
    }
}
