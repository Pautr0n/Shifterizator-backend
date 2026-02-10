package com.shifterizator.shifterizatorbackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIT extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Superadmin can log in and access a protected endpoint")
    void superadminLoginAndAccessProtectedEndpoint() throws Exception {
        // Login with seeded SUPERADMIN user
        LoginRequestDto loginRequest = new LoginRequestDto("superadmin", "SuperAdmin1!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        TokenResponseDto tokenResponse = objectMapper.readValue(loginResponseBody, TokenResponseDto.class);

        assertThat(tokenResponse.accessToken()).isNotBlank();
        assertThat(tokenResponse.username()).isEqualTo("superadmin");

        String bearerToken = "Bearer " + tokenResponse.accessToken();

        // Call /api/auth/me to verify the authenticated user
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("superadmin"));

        // Call a role-protected endpoint requiring SUPERADMIN
        mockMvc.perform(get("/api/test/superadmin")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(content().string("SUPERADMIN OK"));
    }

    @Test
    @DisplayName("Employee cannot access SUPERADMIN-protected endpoint")
    void employeeCannotAccessSuperadminEndpoint() throws Exception {
        // Login with seeded EMPLOYEE user
        LoginRequestDto loginRequest = new LoginRequestDto("employee", "Employee123!");

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

        String bearerToken = "Bearer " + tokenResponse.accessToken();

        // Employee should not have access to SUPERADMIN-only endpoint
        mockMvc.perform(get("/api/test/superadmin")
                        .header("Authorization", bearerToken))
                .andExpect(status().isForbidden());
    }
}

