package com.shifterizator.shifterizatorbackend.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CompanyFlowIT extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String loginAndGetBearerToken(String username, String password) throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(username, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        TokenResponseDto tokenResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );

        assertThat(tokenResponse.accessToken()).isNotBlank();

        return "Bearer " + tokenResponse.accessToken();
    }

    @Test
    @DisplayName("Admin can create and retrieve a company")
    void adminCanCreateAndRetrieveCompany() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");

        CompanyRequestDto request = new CompanyRequestDto(
                "TestCo",
                "Test Company S.A.",
                "A123456789",
                "testco@example.com",
                "123456789",
                "Spain"
        );

        // Create company
        MvcResult createResult = mockMvc.perform(post("/api/companies")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("TestCo"))
                .andReturn();

        CompanyResponseDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                CompanyResponseDto.class
        );

        assertThat(created.id()).isNotNull();
        Long companyId = created.id();

        // Retrieve by id
        mockMvc.perform(get("/api/companies/{id}", companyId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId))
                .andExpect(jsonPath("$.name").value("TestCo"))
                .andExpect(jsonPath("$.email").value("testco@example.com"));

        // List active companies and ensure our company is present
        MvcResult listResult = mockMvc.perform(get("/api/companies/active")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();

        String listJson = listResult.getResponse().getContentAsString();
        CompanyResponseDto[] companies = objectMapper.readValue(
                listJson,
                CompanyResponseDto[].class
        );

        assertThat(companies)
                .extracting(CompanyResponseDto::id)
                .contains(companyId);
    }

    @Test
    @DisplayName("Employee cannot create a company")
    void employeeCannotCreateCompany() throws Exception {
        String employeeToken = loginAndGetBearerToken("employee", "Employee123!");

        CompanyRequestDto request = new CompanyRequestDto(
                "ForbiddenCo",
                "Forbidden Co S.A.",
                "B123456789",
                "forbidden@example.com",
                "987654321",
                "Spain"
        );

        mockMvc.perform(post("/api/companies")
                        .header("Authorization", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

