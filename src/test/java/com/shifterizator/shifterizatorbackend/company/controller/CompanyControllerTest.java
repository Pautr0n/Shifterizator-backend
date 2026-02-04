package com.shifterizator.shifterizatorbackend.company.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.mapper.CompanyMapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WithMockUser
@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyMapper companyMapper;

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
        Mockito.when(companyService.createCompany(any())).thenReturn(company1);
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(companyService).createCompany(any());
        Mockito.verify(companyMapper).toDto(company1);
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void getCompany_should_return_200_and_body() throws Exception {
        Mockito.when(companyService.getCompany(1L)).thenReturn(company1);
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Company 1"));

        Mockito.verify(companyService).getCompany(1L);
        Mockito.verify(companyMapper).toDto(company1);
    }

    // ---------------------------------------------------------
    // LIST ALL
    // ---------------------------------------------------------

    @Test
    void listAllCompanies_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.listAllCompanies()).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        Mockito.verify(companyService).listAllCompanies();
    }

    // ---------------------------------------------------------
    // LIST ACTIVE
    // ---------------------------------------------------------

    @Test
    void listActiveCompanies_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.listActiveCompanies()).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/active").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        Mockito.verify(companyService).listActiveCompanies();
    }

    // ---------------------------------------------------------
    // LIST INACTIVE
    // ---------------------------------------------------------

    @Test
    void listInactiveCompanies_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.listInActiveCompanies()).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/inactive").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));

        Mockito.verify(companyService).listInActiveCompanies();
    }

    // ---------------------------------------------------------
    // SEARCH ACTIVE
    // ---------------------------------------------------------

    @Test
    void searchActiveCompanies_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.searchActiveCompaniesByName("Comp")).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/active/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        Mockito.verify(companyService).searchActiveCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // SEARCH INACTIVE
    // ---------------------------------------------------------

    @Test
    void searchInactiveCompanies_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.searchInActiveCompaniesByName("Comp")).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/inactive/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        Mockito.verify(companyService).searchInActiveCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // SEARCH ALL
    // ---------------------------------------------------------

    @Test
    void searchCompaniesByName_should_return_200_and_list() throws Exception {
        Mockito.when(companyService.searchAllCompaniesByName("Comp")).thenReturn(List.of(company1));
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(get("/api/companies/search?name=Comp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Company 1"));

        Mockito.verify(companyService).searchAllCompaniesByName("Comp");
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void updateCompany_should_return_200_and_body() throws Exception {
        Mockito.when(companyService.updateCompany(eq(1L), any())).thenReturn(company1);
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(put("/api/companies/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(companyService).updateCompany(eq(1L), any());
        Mockito.verify(companyMapper).toDto(company1);
    }

    // ---------------------------------------------------------
    // ACTIVATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void activateCompany_should_return_200_and_body() throws Exception {
        Mockito.when(companyService.activateCompany(1L)).thenReturn(company1);
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(patch("/api/companies/1/activate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(companyService).activateCompany(1L);
    }

    // ---------------------------------------------------------
    // DEACTIVATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void deactivateCompany_should_return_200_and_body() throws Exception {
        Mockito.when(companyService.deactivateCompany(1L)).thenReturn(company1);
        Mockito.when(companyMapper.toDto(company1)).thenReturn(responseDto1);

        mockMvc.perform(patch("/api/companies/1/deactivate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        Mockito.verify(companyService).deactivateCompany(1L);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    @WithMockUser
    void deleteCompany_should_return_204() throws Exception {
        mockMvc.perform(delete("/api/companies/1").with(csrf()))
                .andExpect(status().isNoContent());

        Mockito.verify(companyService).deleteCompany(1L);
    }

}