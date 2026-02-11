package com.shifterizator.shifterizatorbackend.common;

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
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.shift.dto.GenerateMonthRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for pagination and search/filter behavior:
 * verify pagination metadata and filters work correctly across endpoints.
 */
class PaginationAndSearchFlowIT extends BaseIntegrationTest {

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

    private Long createCompany(String adminToken, String name, String country) throws Exception {
        // Company creation is restricted to SUPERADMIN only
        String superAdminToken = loginAndGetBearerToken("superadmin", "SuperAdmin1!");
        String unique = name + country;
        String hash = String.valueOf(unique.hashCode()).replace("-", "");
        String taxId = "T" + hash.substring(0, Math.min(8, hash.length()));
        if (taxId.length() > 12) taxId = taxId.substring(0, 12);
        if (taxId.length() < 9) taxId = String.format("%-9s", taxId).replace(' ', '0');
        CompanyRequestDto request = new CompanyRequestDto(
                name,
                name + " S.A.",
                taxId,
                name.toLowerCase().replace(" ", "") + "@example.com",
                "555444333",
                country
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

    private Long createLocation(String adminToken, Long companyId, String name) throws Exception {
        LocationRequestDto request = new LocationRequestDto(
                name,
                "Address " + name,
                companyId
        );
        MvcResult result = mockMvc.perform(post("/api/locations")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
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
                .andReturn();
        PositionDto position = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PositionDto.class
        );
        return position.id();
    }

    private Long createEmployee(String adminToken, Long companyId, Long positionId, Long locationId, String name, String surname) throws Exception {
        EmployeeRequestDto request = new EmployeeRequestDto(
                name,
                surname,
                name.toLowerCase() + "." + surname.toLowerCase() + "@example.com",
                "123456789",
                positionId,
                Set.of(companyId),
                Set.of(locationId),
                Set.of(),
                null,
                List.of()
        );
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        EmployeeResponseDto employee = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                EmployeeResponseDto.class
        );
        return employee.id();
    }

    @Test
    @DisplayName("Company search with filters and pagination returns correct metadata and filtered results")
    void companySearchWithFiltersAndPagination() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");

        // Use unique suffix to avoid conflicts with other tests (company name must be 4-20 chars)
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(9); // Last 4 digits
        Long company1Id = createCompany(adminToken, "Alpha" + uniqueSuffix, "Spain");
        Long company2Id = createCompany(adminToken, "Beta" + uniqueSuffix, "France");
        Long company3Id = createCompany(adminToken, "Gamma" + uniqueSuffix, "Spain");

        // Company activation/deactivation is restricted to SUPERADMIN only
        String superAdminToken = loginAndGetBearerToken("superadmin", "SuperAdmin1!");
        
        // Deactivate company3 and verify it was deactivated
        MvcResult deactivateResult = mockMvc.perform(patch("/api/companies/{id}/deactivate", company3Id)
                        .header("Authorization", superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(company3Id))
                .andExpect(jsonPath("$.isActive").value(false))
                .andReturn();

        // Verify company3 is actually inactive by fetching it directly
        mockMvc.perform(get("/api/companies/{id}", company3Id)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // Now search for active companies in Spain - should return at least company1, but not company3
        // Note: There might be other companies from other tests, so we verify our specific companies
        MvcResult result = mockMvc.perform(get("/api/companies")
                        .header("Authorization", adminToken)
                        .param("country", "Spain")
                        .param("isActive", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
        
        // Verify company1 is in the results and is active
        boolean foundCompany1 = false;
        boolean foundCompany3 = false;
        for (com.fasterxml.jackson.databind.JsonNode company : root.get("content")) {
            long id = company.get("id").asLong();
            if (id == company1Id) {
                foundCompany1 = true;
                assertThat(company.get("isActive").asBoolean()).isTrue();
                assertThat(company.get("country").asText()).isEqualTo("Spain");
            }
            if (id == company3Id) {
                foundCompany3 = true;
            }
        }
        
        // Verify our expectations: company1 should be found, company3 should NOT be found
        assertThat(foundCompany1).as("Company1 (active, Spain) should be in search results").isTrue();
        assertThat(foundCompany3).as("Company3 (deactivated) should NOT be in active search results").isFalse();
    }

    @Test
    @DisplayName("Employee search with filters and pagination returns correct metadata and filtered results")
    void employeeSearchWithFiltersAndPagination() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");

        Long company1Id = createCompany(adminToken, "SearchCo1", "Spain");
        Long company2Id = createCompany(adminToken, "SearchCo2", "France");
        Long location1Id = createLocation(adminToken, company1Id, "Loc1");
        Long location2Id = createLocation(adminToken, company2Id, "Loc2");
        Long position1Id = createPosition(adminToken, company1Id, "Cashier");
        Long position2Id = createPosition(adminToken, company2Id, "Manager");

        Long emp1Id = createEmployee(adminToken, company1Id, position1Id, location1Id, "Alice", "Smith");
        Long emp2Id = createEmployee(adminToken, company1Id, position1Id, location1Id, "Bob", "Jones");
        Long emp3Id = createEmployee(adminToken, company2Id, position2Id, location2Id, "Charlie", "Brown");

        MvcResult result = mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("companyId", String.valueOf(company1Id))
                        .param("name", "Alice")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(emp1Id))
                .andExpect(jsonPath("$.content[0].name").value("Alice"))
                .andReturn();

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("locationId", String.valueOf(location1Id))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.id == " + emp1Id + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.id == " + emp2Id + ")]").exists());

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", adminToken)
                        .param("position", "Cashier")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[?(@.id == " + emp1Id + ")]").exists())
                .andExpect(jsonPath("$.content[?(@.id == " + emp2Id + ")]").exists());
    }

    @Test
    @DisplayName("Shift instances search with date range and pagination returns correct metadata")
    void shiftInstancesSearchWithDateRangeAndPagination() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "ShiftSearch", "Spain");
        Long locationId = createLocation(adminToken, companyId, "ShiftLoc");
        Long positionId = createPosition(adminToken, companyId, "Worker");

        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Search shift",
                null,
                null,
                true
        );
        mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated());

        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, 2026, 11);
        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated());

        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 15);

        MvcResult result = mockMvc.perform(get("/api/shift-instances")
                        .header("Authorization", adminToken)
                        .param("locationId", String.valueOf(locationId))
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
        int totalElements = root.get("totalElements").asInt();
        assertThat(totalElements).isGreaterThanOrEqualTo(15);
        assertThat(root.get("content").size()).isLessThanOrEqualTo(5);

        List<ShiftInstanceResponseDto> instances = objectMapper.readValue(
                root.get("content").toString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        for (ShiftInstanceResponseDto instance : instances) {
            assertThat(instance.date()).isAfterOrEqualTo(startDate).isBeforeOrEqualTo(endDate);
            assertThat(instance.locationId()).isEqualTo(locationId);
        }
    }
}
