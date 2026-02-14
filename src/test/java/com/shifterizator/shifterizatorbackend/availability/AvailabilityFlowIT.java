package com.shifterizator.shifterizatorbackend.availability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AvailabilityFlowIT extends BaseIntegrationTest {

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
        String name = "AvailCo-" + suffix;
        String legalName = "Avail Company " + suffix + " S.A.";
        String taxId = "C" + suffix + "23456789";
        String email = "availco" + suffix + "@example.com";
        CompanyRequestDto request = new CompanyRequestDto(
                name,
                legalName,
                taxId,
                email,
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

    private Long createPosition(String adminToken, Long companyId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/positions")
                        .header("Authorization", adminToken)
                        .param("name", "Worker")
                        .param("companyId", String.valueOf(companyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        com.shifterizator.shifterizatorbackend.employee.dto.PositionDto position =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        com.shifterizator.shifterizatorbackend.employee.dto.PositionDto.class);

        return position.id();
    }

    private Long createEmployee(String adminToken, Long companyId, Long positionId) throws Exception {
        EmployeeRequestDto employeeRequest = new EmployeeRequestDto(
                "Alice",
                "Worker",
                "alice.worker@example.com",
                "111222333",
                positionId,
                Set.of(companyId),
                Set.of(),
                Set.of(),
                null,
                5,
                List.of(),
                null
        );

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        EmployeeResponseDto employee = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                EmployeeResponseDto.class
        );

        return employee.id();
    }

    @Test
    @DisplayName("Admin can create availability for an employee and retrieve it")
    void adminCanCreateAndRetrieveAvailability() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");

        Long companyId = createCompany(adminToken, "1");
        Long positionId = createPosition(adminToken, companyId);
        Long employeeId = createEmployee(adminToken, companyId, positionId);

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        AvailabilityRequestDto availabilityRequest = new AvailabilityRequestDto(
                employeeId,
                start,
                end,
                AvailabilityType.AVAILABLE
        );

        // Create availability
        MvcResult createResult = mockMvc.perform(post("/api/availability")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andReturn();

        AvailabilityResponseDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                AvailabilityResponseDto.class
        );

        Long availabilityId = created.id();

        // Get by id
        mockMvc.perform(get("/api/availability/{id}", availabilityId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(availabilityId))
                .andExpect(jsonPath("$.employeeId").value(employeeId));

        // Get by-employee
        mockMvc.perform(get("/api/availability/by-employee/{employeeId}", employeeId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(employeeId));
    }

    @Test
    @DisplayName("Employee cannot create availability for another employee")
    void employeeCannotCreateAvailabilityForAnotherEmployee() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        String employeeToken = loginAndGetBearerToken("employee", "Employee123!");

        Long companyId = createCompany(adminToken, "2");
        Long positionId = createPosition(adminToken, companyId);
        Long anotherEmployeeId = createEmployee(adminToken, companyId, positionId);

        AvailabilityRequestDto availabilityRequest = new AvailabilityRequestDto(
                anotherEmployeeId,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(4),
                AvailabilityType.AVAILABLE
        );

        mockMvc.perform(post("/api/availability")
                        .header("Authorization", employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availabilityRequest)))
                .andExpect(status().isForbidden());
    }
}

