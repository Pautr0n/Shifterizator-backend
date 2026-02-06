package com.shifterizator.shifterizatorbackend.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.exception.CompanyValidationException;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser
@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyMapper companyMapper;

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


    private Company company1;
    private CompanyResponseDto responseDto1;
    private CompanyRequestDto requestDto;

    @BeforeEach
    void setup() {
        company1 = new Company("Company 1"
                , "Legal Company 1"
                , "12345678A"
                , "company1@company.com"
                , "+341111111");
        company1.setId(1L);

        responseDto1 = new CompanyResponseDto(1L
                , "Company 1"
                , "Legal Company 1"
                , "12345678A"
                , "company1@company.com"
                , "+341111111"
                , true
                , LocalDateTime.of(2025, 12, 1, 17, 25)
                , LocalDateTime.of(2025, 12, 7, 14, 10));

        requestDto = new CompanyRequestDto("Company 1"
                , "Legal Company 1"
                , "12345678A"
                , "company1@company.com"
                , "+341111111");
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Test
    void createCompany_should_return_201_and_body() throws Exception {
        when(companyService.createCompany(any())).thenReturn(company1);
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(companyService).createCompany(any());
        verify(companyMapper).toDto(company1);
    }

    @Test
    void createCompany_should_return_400_when_validation_error() throws Exception {
        when(companyService.createCompany(any()))
                .thenThrow(new CompanyValidationException("Company name already exists: Company 1"));

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company name already exists: Company 1"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(companyService).createCompany(any());
    }


    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void getCompany_should_return_200_and_body() throws Exception {
        when(companyService.getCompany(1L)).thenReturn(company1);
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Company 1"));

        verify(companyService).getCompany(1L);
        verify(companyMapper).toDto(company1);
    }

    @Test
    void getCompany_should_return_404_when_not_found() throws Exception {
        when(companyService.getCompany(99L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 99"));

        mockMvc.perform(get("/api/companies/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 99"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(companyService).getCompany(99L);
    }

    // ---------------------------------------------------------
    // LIST ALL
    // ---------------------------------------------------------

    @Test
    void listAllCompanies_should_return_200_and_list() throws Exception {
        when(companyService.listAllCompanies()).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(companyService).listAllCompanies();
    }

    // ---------------------------------------------------------
    // LIST ACTIVE
    // ---------------------------------------------------------

    @Test
    void listActiveCompanies_should_return_200_and_list() throws Exception {
        when(companyService.listActiveCompanies()).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/active").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(companyService).listActiveCompanies();
    }

    // ---------------------------------------------------------
    // LIST INACTIVE
    // ---------------------------------------------------------

    @Test
    void listInactiveCompanies_should_return_200_and_list() throws Exception {
        when(companyService.listInActiveCompanies()).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/inactive").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(companyService).listInActiveCompanies();
    }

    // ---------------------------------------------------------
    // SEARCH ACTIVE
    // ---------------------------------------------------------

    @Test
    void searchActiveCompanies_should_return_200_and_list() throws Exception {
        when(companyService.searchActiveCompaniesByName("Comp")).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/active/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        verify(companyService).searchActiveCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // SEARCH INACTIVE
    // ---------------------------------------------------------

    @Test
    void searchInactiveCompanies_should_return_200_and_list() throws Exception {
        when(companyService.searchInActiveCompaniesByName("Comp")).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/inactive/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        verify(companyService).searchInActiveCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // SEARCH ALL
    // ---------------------------------------------------------

    @Test
    void searchCompaniesByName_should_return_200_and_list() throws Exception {
        when(companyService.searchAllCompaniesByName("Comp")).thenReturn(List.of(company1));
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        verify(companyService).searchAllCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void updateCompany_should_return_200_and_body() throws Exception {
        when(companyService.updateCompany(eq(1L), any())).thenReturn(company1);
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(put("/api/companies/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(companyService).updateCompany(eq(1L), any());
        verify(companyMapper).toDto(company1);
    }

    @Test
    void updateCompany_should_return_404_when_not_found() throws Exception {
        when(companyService.updateCompany(eq(99L), any()))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 99"));

        mockMvc.perform(put("/api/companies/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 99"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(companyService).updateCompany(eq(99L), any());
    }

    @Test
    void updateCompany_should_return_400_when_validation_error() throws Exception {
        when(companyService.updateCompany(eq(1L), any()))
                .thenThrow(new CompanyValidationException("Company email already exists: company1@company.com"));

        mockMvc.perform(put("/api/companies/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Company email already exists: company1@company.com"))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(companyService).updateCompany(eq(1L), any());
    }

    // ---------------------------------------------------------
    // ACTIVATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void activateCompany_should_return_200_and_body() throws Exception {
        when(companyService.activateCompany(1L)).thenReturn(company1);
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(patch("/api/companies/1/activate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(companyService).activateCompany(1L);
    }

    @Test
    void activateCompany_should_return_404_when_not_found() throws Exception {
        when(companyService.activateCompany(99L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 99"));

        mockMvc.perform(patch("/api/companies/99/activate").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 99"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(companyService).activateCompany(99L);
    }

    // ---------------------------------------------------------
    // DEACTIVATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void deactivateCompany_should_return_200_and_body() throws Exception {
        when(companyService.deactivateCompany(1L)).thenReturn(company1);
        when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(patch("/api/companies/1/deactivate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(companyService).deactivateCompany(1L);
    }

    @Test
    void deactivateCompany_should_return_404_when_not_found() throws Exception {
        when(companyService.deactivateCompany(99L))
                .thenThrow(new CompanyNotFoundException("Company not found with id: 99"));

        mockMvc.perform(patch("/api/companies/99/deactivate").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 99"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(companyService).deactivateCompany(99L);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void deleteCompany_should_return_204() throws Exception {
        mockMvc.perform(delete("/api/companies/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(companyService).deleteCompany(1L);
    }

    @Test
    void deleteCompany_should_return_404_when_not_found() throws Exception {
        doThrow(new CompanyNotFoundException("Company not found with id: 99"))
                .when(companyService).deleteCompany(99L);

        mockMvc.perform(delete("/api/companies/99").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found with id: 99"))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        verify(companyService).deleteCompany(99L);
    }

}