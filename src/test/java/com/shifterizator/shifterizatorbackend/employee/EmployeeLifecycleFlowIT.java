package com.shifterizator.shifterizatorbackend.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeePreferencesResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmployeeLifecycleFlowIT extends BaseIntegrationTest {

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
        String superAdminToken = loginAndGetBearerToken("superadmin", "SuperAdmin1!");
        String taxId = suffix.length() <= 2 ? "S" + suffix + "23456789" : suffix.substring(0, 1).toUpperCase() + "12345678";
        if (taxId.length() > 12) taxId = taxId.substring(0, 12);
        if (taxId.length() < 9) taxId = String.format("%-9s", taxId).replace(' ', '0');
        CompanyRequestDto request = new CompanyRequestDto(
                "EmpLifeCo-" + suffix,
                "EmpLife Company " + suffix + " S.A.",
                taxId,
                "emplife" + suffix + "@example.com",
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

    private Long createLocation(String adminToken, Long companyId, String suffix) throws Exception {
        LocationRequestDto request = new LocationRequestDto(
                "EmpLoc-" + suffix,
                "Address " + suffix,
                companyId,
                null,
                null
        );
        MvcResult result = mockMvc.perform(post("/api/locations")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        LocationResponseDto location = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LocationResponseDto.class
        );
        return location.id();
    }

    private Long createPosition(String adminToken, Long companyId, String positionName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/positions")
                        .header("Authorization", adminToken)
                        .param("name", positionName)
                        .param("companyId", String.valueOf(companyId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        PositionDto position = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PositionDto.class
        );
        return position.id();
    }

    @Test
    @DisplayName("Admin can create employee, update preferences, and search by various criteria")
    void adminCanCreateEmployeeUpdatePreferencesAndSearch() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "life");
        Long locationId = createLocation(adminToken, companyId, "life");
        Long positionId = createPosition(adminToken, companyId, "Cashier");

        EmployeeRequestDto createRequest = new EmployeeRequestDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "123456789",
                positionId,
                Set.of(companyId),
                Set.of(locationId),
                Set.of(),
                "FRIDAY",
                5,
                List.of(),
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/employees")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.preferredDayOff").value("FRIDAY"))
                .andReturn();

        EmployeeResponseDto created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                EmployeeResponseDto.class
        );
        Long employeeId = created.id();
        assertThat(created.preferredDayOff()).isEqualTo("FRIDAY");
        assertThat(created.preferredShiftTemplateIds()).isEmpty();

        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Morning shift",
                null,
                null,
                null,
                true,
                null
        );
        MvcResult templateResult = mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        ShiftTemplateResponseDto template = objectMapper.readValue(
                templateResult.getResponse().getContentAsString(),
                ShiftTemplateResponseDto.class
        );
        Long templateId = template.id();

        EmployeePreferencesRequestDto preferencesRequest = new EmployeePreferencesRequestDto(
                "WEDNESDAY",
                List.of(templateId),
                5
        );
        MvcResult preferencesResult = mockMvc.perform(put("/api/employees/{id}/preferences", employeeId)
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(preferencesRequest)))
                .andExpect(status().isOk())
                .andReturn();
        EmployeePreferencesResponseDto preferences = objectMapper.readValue(
                preferencesResult.getResponse().getContentAsString(),
                EmployeePreferencesResponseDto.class
        );
        assertThat(preferences.preferredDayOff()).isEqualTo("WEDNESDAY");
        assertThat(preferences.preferredShiftTemplateIds()).containsExactly(templateId);

        mockMvc.perform(get("/api/employees/{id}/preferences", employeeId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredDayOff").value("WEDNESDAY"))
                .andExpect(jsonPath("$.preferredShiftTemplateIds[0]").value(templateId));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("companyId", String.valueOf(companyId))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + employeeId + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'John')]").exists());

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("locationId", String.valueOf(locationId))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + employeeId + ")]").exists());

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("name", "John")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + employeeId + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.surname == 'Doe')]").exists());

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("position", "Cashier")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + employeeId + ")]").exists());

        mockMvc.perform(get("/api/employees/{id}", employeeId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.preferredDayOff").value("WEDNESDAY"))
                .andExpect(jsonPath("$.preferredShiftTemplateIds[0]").value(templateId));
    }
}
