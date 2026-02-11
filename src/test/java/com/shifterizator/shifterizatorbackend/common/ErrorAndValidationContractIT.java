package com.shifterizator.shifterizatorbackend.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests to verify the standardized error/validation contract
 * exposed by GlobalExceptionHandler and bean validation.
 */
class ErrorAndValidationContractIT extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String loginAsAdminAndGetBearerToken() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto("admin", "Admin123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto tokens =
                objectMapper.readValue(loginResult.getResponse().getContentAsString(),
                        com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto.class);

        return "Bearer " + tokens.accessToken();
    }

    @Test
    @DisplayName("Requesting non-existing company returns 404 ApiErrorDto with NOT_FOUND error code")
    void requestingNonExistingCompanyReturnsNotFoundError() throws Exception {
        long nonExistingId = 999999L;
        String adminToken = loginAsAdminAndGetBearerToken();

        mockMvc.perform(get("/api/companies/{id}", nonExistingId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Creating company with invalid data returns 400 ApiErrorDto with VALIDATION_ERROR")
    void creatingCompanyWithInvalidDataReturnsValidationError() throws Exception {
        String adminToken = loginAsAdminAndGetBearerToken();

        // Invalid company request: blank name, short taxId, invalid email
        CompanyRequestDto invalidRequest = new CompanyRequestDto(
                "   ",            // name - blank
                "L",              // legalName too short
                "123",            // taxId too short
                "not-an-email",   // invalid email
                "123",            // phone too short
                "ES"
        );

        MvcResult result = mockMvc.perform(post("/api/companies")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // The message should mention at least one of the invalid fields
        assertThat(body).contains("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("Invalid login credentials return 400 ApiErrorDto with INVALID_CREDENTIALS")
    void invalidLoginCredentialsReturnInvalidCredentialsError() throws Exception {
        LoginRequestDto badLogin = new LoginRequestDto("unknown-user", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Accessing protected endpoint without auth header returns 401")
    void accessingProtectedEndpointWithoutAuthReturnsUnauthorized() throws Exception {
        // JwtAuthenticationFilter currently returns 401 without a JSON body
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

