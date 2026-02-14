package com.shifterizator.shifterizatorbackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoleAccessMatrixIT extends BaseIntegrationTest {

    private enum TestRole {
        SUPERADMIN("superadmin", "SuperAdmin1!"),
        COMPANYADMIN("admin", "Admin123!"),
        SHIFTMANAGER("manager", "Manager123!"),
        EMPLOYEE("employee", "Employee123!");

        private final String username;
        private final String password;

        TestRole(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private record EndpointExpectation(
            String name,
            String httpMethod,
            String path,
            Set<TestRole> allowedRoles
    ) {
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final List<EndpointExpectation> protectedEndpoints = List.of(
            new EndpointExpectation(
                    "Companies search",
                    "GET",
                    "/api/companies",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Companies create",
                    "POST",
                    "/api/companies",
                    Set.of(TestRole.SUPERADMIN)
            ),
            new EndpointExpectation(
                    "Companies delete",
                    "DELETE",
                    "/api/companies/1",
                    Set.of(TestRole.SUPERADMIN)
            ),
            new EndpointExpectation(
                    "Availability search",
                    "GET",
                    "/api/availability",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Availability create",
                    "POST",
                    "/api/availability",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Employees search",
                    "GET",
                    "/api/employees",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Employees create",
                    "POST",
                    "/api/employees",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Employees update",
                    "PUT",
                    "/api/employees/1",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Positions by company",
                    "GET",
                    "/api/positions/company/1",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Positions create",
                    "POST",
                    "/api/positions",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Users search",
                    "GET",
                    "/api/users",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Users create",
                    "POST",
                    "/api/users",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Locations by company",
                    "GET",
                    "/api/locations/company/1",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Locations create",
                    "POST",
                    "/api/locations",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Shift templates search",
                    "GET",
                    "/api/shift-templates",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Shift templates create",
                    "POST",
                    "/api/shift-templates",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Shift templates update",
                    "PUT",
                    "/api/shift-templates/1",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Shift instances search",
                    "GET",
                    "/api/shift-instances",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Shift instances generate month",
                    "POST",
                    "/api/shift-instances/generate-month",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Shift assignments by employee",
                    "GET",
                    "/api/shift-assignments/employee/1",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Shift assignments create",
                    "POST",
                    "/api/shift-assignments",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Blackout days search",
                    "GET",
                    "/api/blackout-days",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Blackout days create",
                    "POST",
                    "/api/blackout-days",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Special opening hours search",
                    "GET",
                    "/api/opening-hours",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Opening hours create",
                    "POST",
                    "/api/opening-hours",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Languages list",
                    "GET",
                    "/api/languages",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Languages create",
                    "POST",
                    "/api/languages",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Auth me",
                    "GET",
                    "/api/auth/me",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            ),
            new EndpointExpectation(
                    "Test SUPERADMIN endpoint",
                    "GET",
                    "/api/test/superadmin",
                    Set.of(TestRole.SUPERADMIN)
            ),
            new EndpointExpectation(
                    "Test COMPANYADMIN endpoint",
                    "GET",
                    "/api/test/companyadmin",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN)
            ),
            new EndpointExpectation(
                    "Test SHIFTMANAGER endpoint",
                    "GET",
                    "/api/test/shiftmanager",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER)
            ),
            new EndpointExpectation(
                    "Test EMPLOYEE endpoint",
                    "GET",
                    "/api/test/employee",
                    Set.of(TestRole.SUPERADMIN, TestRole.COMPANYADMIN, TestRole.SHIFTMANAGER, TestRole.EMPLOYEE)
            )
    );

    private final List<String> publicGetEndpoints = List.of(
            "/api/health"
    );

    private final Map<TestRole, String> bearerTokens = new EnumMap<>(TestRole.class);

    @BeforeAll
    void loginAllRoles() throws Exception {
        for (TestRole role : TestRole.values()) {
            LoginRequestDto request = new LoginRequestDto(role.username, role.password);

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            TokenResponseDto tokens = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    TokenResponseDto.class
            );

            bearerTokens.put(role, "Bearer " + tokens.accessToken());
        }
    }

    @Test
    @DisplayName("Public GET endpoints are accessible without authentication")
    void publicEndpointsAccessibleAnonymously() throws Exception {
        for (String path : publicGetEndpoints) {
            int statusCode = mockMvc.perform(get(path))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assertThat(statusCode)
                    .as("Expected anonymous access to %s to be allowed (2xx/3xx/4xx but not 401/403)", path)
                    .isNotIn(401, 403);
        }
    }

    @Test
    @DisplayName("Protected endpoints enforce role-based access according to SecurityConfig")
    void protectedEndpointsEnforceRoleMatrix() throws Exception {
        for (EndpointExpectation endpoint : protectedEndpoints) {
            for (TestRole role : TestRole.values()) {
                String token = bearerTokens.get(role);

                MockHttpServletRequestBuilder requestBuilder;
                switch (endpoint.httpMethod()) {
                    case "GET" -> requestBuilder = get(endpoint.path());
                    case "POST" -> requestBuilder = post(endpoint.path())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}");
                    case "PUT" -> requestBuilder = put(endpoint.path())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}");
                    case "DELETE" -> requestBuilder = delete(endpoint.path());
                    default -> requestBuilder = get(endpoint.path());
                }

                requestBuilder.header("Authorization", token);

                int statusCode = mockMvc.perform(requestBuilder)
                        .andReturn()
                        .getResponse()
                        .getStatus();

                boolean isAllowed = endpoint.allowedRoles().contains(role);

                if (isAllowed) {
                    assertThat(statusCode)
                            .as("Expected role %s to be allowed on %s %s but got %s",
                                    role.name(), endpoint.httpMethod(), endpoint.path(), statusCode)
                            .isNotIn(401, 403);
                } else {
                    assertThat(statusCode)
                            .as("Expected role %s to be FORBIDDEN on %s %s but got %s",
                                    role.name(), endpoint.httpMethod(), endpoint.path(), statusCode)
                            .isEqualTo(403);
                }
            }
        }
    }

    @Test
    @DisplayName("Anonymous users are rejected on protected endpoints")
    void anonymousRejectedOnProtectedEndpoints() throws Exception {
        for (EndpointExpectation endpoint : protectedEndpoints) {
            int statusCode = mockMvc.perform(get(endpoint.path()))
                    .andReturn()
                    .getResponse()
                    .getStatus();

            assertThat(statusCode)
                    .as("Expected anonymous user to be rejected on %s %s", endpoint.httpMethod(), endpoint.path())
                    .isEqualTo(401);
        }
    }
}

