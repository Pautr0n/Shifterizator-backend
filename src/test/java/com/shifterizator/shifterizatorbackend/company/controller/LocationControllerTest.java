package com.shifterizator.shifterizatorbackend.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.service.LocationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private LocationMapper locationMapper;

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

    private Location location;
    private LocationResponseDto responseDto;
    private LocationRequestDto requestDto;
    private Company company;

    @BeforeEach
    void setup() {
        company = new Company("Company 1", "Legal 1", "12345678A", "c1@c.com", "+341111111");
        company.setId(1L);

        location = Location.builder()
                .id(10L)
                .name("Headquarters")
                .address("Main Street 1")
                .company(company)
                .build();

        responseDto = new LocationResponseDto(10L, "Headquarters", "Main Street 1", 1L);
        requestDto = new LocationRequestDto("Headquarters", "Main Street 1", 1L);
    }

    @Test
    void create_should_return_201_and_body() throws Exception {
        when(locationService.create(any())).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        mockMvc.perform(post("/api/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Headquarters"))
                .andExpect(jsonPath("$.address").value("Main Street 1"))
                .andExpect(jsonPath("$.companyId").value(1));

        verify(locationService).create(any());
        verify(locationMapper).toDto(location);
    }

    @Test
    void create_should_return_404_when_company_not_found() throws Exception {
        when(locationService.create(any()))
                .thenThrow(new CompanyNotFoundException("Company not found"));

        mockMvc.perform(post("/api/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(locationService).create(any());
    }

    @Test
    void create_should_return_400_when_validation_error() throws Exception {
        LocationRequestDto invalidDto = new LocationRequestDto("", "addr", null);

        mockMvc.perform(post("/api/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).create(any());
    }

    @Test
    void update_should_return_200_and_body() throws Exception {
        when(locationService.update(eq(10L), any())).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        mockMvc.perform(put("/api/locations/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Headquarters"));

        verify(locationService).update(eq(10L), any());
        verify(locationMapper).toDto(location);
    }

    @Test
    void update_should_return_404_when_location_not_found() throws Exception {
        when(locationService.update(eq(99L), any()))
                .thenThrow(new LocationNotFoundException("Location not found"));

        mockMvc.perform(put("/api/locations/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(locationService).update(eq(99L), any());
    }

    @Test
    void update_should_return_400_when_validation_error() throws Exception {
        LocationRequestDto invalidDto = new LocationRequestDto("ab", "addr", 1L);

        mockMvc.perform(put("/api/locations/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).update(any(), any());
    }

    @Test
    void delete_should_return_204() throws Exception {
        mockMvc.perform(delete("/api/locations/10").with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationService).delete(10L);
    }

    @Test
    void delete_should_return_404_when_not_found() throws Exception {
        doThrow(new LocationNotFoundException("Location not found"))
                .when(locationService).delete(99L);

        mockMvc.perform(delete("/api/locations/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(locationService).delete(99L);
    }

    @Test
    void findById_should_return_200_and_body() throws Exception {
        when(locationService.findById(10L)).thenReturn(location);
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        mockMvc.perform(get("/api/locations/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Headquarters"));

        verify(locationService).findById(10L);
        verify(locationMapper).toDto(location);
    }

    @Test
    void findById_should_return_404_when_not_found() throws Exception {
        when(locationService.findById(99L))
                .thenThrow(new LocationNotFoundException("Location not found"));

        mockMvc.perform(get("/api/locations/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Location not found"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(locationService).findById(99L);
    }

    @Test
    void findByCompany_should_return_200_and_list() throws Exception {
        when(locationService.findByCompany(1L)).thenReturn(List.of(location));
        when(locationMapper.toDto(location)).thenReturn(responseDto);

        mockMvc.perform(get("/api/locations/company/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].name").value("Headquarters"));

        verify(locationService).findByCompany(1L);
    }

    @Test
    void findByCompany_should_return_200_and_empty_list() throws Exception {
        when(locationService.findByCompany(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/locations/company/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(locationService).findByCompany(1L);
    }
}
