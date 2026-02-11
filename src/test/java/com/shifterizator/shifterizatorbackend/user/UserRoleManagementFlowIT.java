package com.shifterizator.shifterizatorbackend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for user and role management:
 * create user → update role/activation → system user protection → role-based access.
 */
class UserRoleManagementFlowIT extends BaseIntegrationTest {

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

    private Long createCompany(String adminToken, String suffix) throws Exception {
        // Company creation is restricted to SUPERADMIN only
        String superAdminToken = loginAndGetBearerToken("superadmin", "SuperAdmin1!");
        String taxId = suffix.length() <= 2 ? "S" + suffix + "23456789" : suffix.substring(0, 1).toUpperCase() + "12345678";
        if (taxId.length() > 12) taxId = taxId.substring(0, 12);
        if (taxId.length() < 9) taxId = String.format("%-9s", taxId).replace(' ', '0');
        CompanyRequestDto request = new CompanyRequestDto(
                "UserMgmtCo-" + suffix,
                "UserMgmt Company " + suffix + " S.A.",
                taxId,
                "usermgmt" + suffix + "@example.com",
                "555444333",
                "Spain"
        );
        MvcResult result = mockMvc.perform(post("/api/companies")
                        .header("Authorization", superAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        CompanyResponseDto company = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CompanyResponseDto.class
        );
        return company.id();
    }

    @Test
    @DisplayName("Admin can create user with company role, update role and activation status")
    void adminCanCreateUserUpdateRoleAndActivation() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "user");

        UserRequestDto createRequest = new UserRequestDto(
                "newuser",
                "newuser@example.com",
                "Password123!",
                "EMPLOYEE",
                companyId,
                "123456789"
        );

        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.companyId").value(companyId))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        UserResponseDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                UserResponseDto.class
        );
        Long userId = created.id();

        UserRequestDto updateRequest = new UserRequestDto(
                "newuser",
                "newuser@example.com",
                "Password123!",
                "SHIFTMANAGER",
                companyId,
                "123456789"
        );
        mockMvc.perform(put("/api/users/{id}", userId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SHIFTMANAGER"));

        mockMvc.perform(patch("/api/users/{id}/deactivate", userId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        mockMvc.perform(patch("/api/users/{id}/activate", userId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        mockMvc.perform(get("/api/users/company/{companyId}", companyId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + userId + ")]").exists());
    }

    @Test
    @DisplayName("System user (superadmin) cannot be deleted")
    void systemUserCannotBeDeleted() throws Exception {
        String superadminToken = loginAndGetBearerToken("superadmin", "SuperAdmin1!");

        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .header("Authorization", superadminToken)
                        .param("role", "SUPERADMIN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        String responseBody = listResult.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
        com.fasterxml.jackson.databind.JsonNode content = root.get("content");
        List<UserResponseDto> users = objectMapper.readValue(
                content.toString(),
                objectMapper.getTypeFactory().constructCollectionType(
                        java.util.List.class,
                        UserResponseDto.class
                )
        );

        Long systemSuperadminId = null;
        for (UserResponseDto user : users) {
            if ("superadmin".equals(user.username())) {
                systemSuperadminId = user.id();
                break;
            }
        }
        assertThat(systemSuperadminId).isNotNull();

        MvcResult deleteResult = mockMvc.perform(delete("/api/users/{id}", systemSuperadminId)
                        .header("Authorization", superadminToken))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(deleteResult.getResponse().getContentAsString()).contains("System user cannot be deleted");
    }

    @Test
    @DisplayName("Employee cannot modify users but can read them")
    void employeeCannotModifyUsersButCanReadThem() throws Exception {
        String employeeToken = loginAndGetBearerToken("employee", "Employee123!");

        UserRequestDto createRequest = new UserRequestDto(
                "testuser",
                "test@example.com",
                "Password123!",
                "EMPLOYEE",
                1L,
                null
        );

        // Employee cannot create users (POST requires SUPERADMIN or COMPANYADMIN)
        mockMvc.perform(post("/api/users")
                        .header("Authorization", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // Employee can read users (GET is allowed for all authenticated)
        mockMvc.perform(get("/api/users")
                        .header("Authorization", employeeToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
