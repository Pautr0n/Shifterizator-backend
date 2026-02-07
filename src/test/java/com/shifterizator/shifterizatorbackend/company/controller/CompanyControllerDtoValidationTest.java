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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;


@WithMockUser
@WebMvcTest(CompanyController.class)
public class CompanyControllerDtoValidationTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private CompanyMapper companyMapper;

    @MockitoBean
    private com.shifterizator.shifterizatorbackend.employee.mapper.EmployeeMapper employeeMapper;

    @MockitoBean
    private com.shifterizator.shifterizatorbackend.company.mapper.LocationMapper locationMapper;

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
                , "ES"
                , true
                , LocalDateTime.of(2025, 12, 1, 17, 25)
                , LocalDateTime.of(2025, 12, 7, 14, 10)
                , null
                , null);

        requestDto = new CompanyRequestDto("Company 1"
                , "Legal Company 1"
                , "12345678A"
                , "company1@company.com"
                , "+341111111"
                , "ES");
    }

    @Test
    void createCompany_should_return_400_when_name_is_blank() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "",
                "Legal Company 1",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_name_is_less_than_4_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "123",
                "Legal Company 1",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_name_is_more_than_20_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "123456789012345678901",
                "Legal Company 1",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_legalName_is_blank() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_legalName_is_less_than_4_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "123",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_legalName_is_more_than_50_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "123456789012345678901234567890123456789012345678901234567890",
                "12345678A",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_email_invalid() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "12345678A",
                "not-an-email",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_email_is_blank() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "12345678A",
                "",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_taxId_is_blank() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_taxId_is_less_than_9_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "12345678",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_taxId_is_more_than_12_chars() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "1234567890123",
                "company1@company.com",
                "+341111111",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

    @Test
    void createCompany_should_return_400_when_phone_is_blank() throws Exception {
        CompanyRequestDto invalidDto = new CompanyRequestDto(
                "Company 1",
                "Legal Company 1",
                "12345678A",
                "company1@company.com",
                "",
                null
        );

        mockMvc.perform(post("/api/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());

        Mockito.verifyNoInteractions(companyService);
    }

}
