package com.shifterizator.shifterizatorbackend.shift;

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
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.shift.dto.GenerateMonthRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftAssignmentResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShiftFlowIT extends BaseIntegrationTest {

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
                "ShiftCo-" + suffix,
                "Shift Company " + suffix + " S.A.",
                taxId,
                "shiftco" + suffix + "@example.com",
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
                "Loc-" + suffix,
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

    private Long createPosition(String adminToken, Long companyId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/positions")
                        .header("Authorization", adminToken)
                        .param("name", "Worker")
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

    private Long createEmployee(String adminToken, Long companyId, Long positionId, Long locationId, String suffix) throws Exception {
        EmployeeRequestDto request = new EmployeeRequestDto(
                "ShiftEmp-" + suffix,
                "Worker",
                "shiftemp" + suffix + "@example.com",
                "111222333",
                positionId,
                Set.of(companyId),
                Set.of(locationId),
                Set.of(),
                null,
                5,
                List.of(),
                null,
                null
        );
        MvcResult result = mockMvc.perform(post("/api/employees")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
    @DisplayName("Admin can create shift template, generate instances for a month, and retrieve by location and date range")
    void adminCanCreateTemplateGenerateMonthAndRetrieveInstances() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "1");
        Long locationId = createLocation(adminToken, companyId, "1");
        Long positionId = createPosition(adminToken, companyId);

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                startTime,
                endTime,
                "Morning shift",
                null,
                null,
                true,
                null
        );

        MvcResult createTemplateResult = mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.locationId").value(locationId))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists())
                .andReturn();

        ShiftTemplateResponseDto template = objectMapper.readValue(
                createTemplateResult.getResponse().getContentAsString(),
                ShiftTemplateResponseDto.class
        );
        Long templateId = template.id();
        assertThat(templateId).isNotNull();

        int year = 2026;
        int month = 6;
        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, year, month);

        MvcResult generateResult = mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String generateBody = generateResult.getResponse().getContentAsString();
        List<ShiftInstanceResponseDto> generated = objectMapper.readValue(
                generateBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );

        LocalDate rangeStart = LocalDate.of(year, month, 1);
        LocalDate rangeEnd = rangeStart.withDayOfMonth(rangeStart.lengthOfMonth());

        assertThat(generated).isNotEmpty();
        ShiftInstanceResponseDto first = generated.get(0);
        assertThat(first.shiftTemplateId()).isEqualTo(templateId);
        assertThat(first.locationId()).isEqualTo(locationId);
        assertThat(first.date()).isAfterOrEqualTo(rangeStart).isBeforeOrEqualTo(rangeEnd);
        assertThat(first.startTime()).isEqualTo(startTime);
        assertThat(first.endTime()).isEqualTo(endTime);

        mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/range", locationId)
                        .header("Authorization", adminToken)
                        .param("startDate", rangeStart.toString())
                        .param("endDate", rangeEnd.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shiftTemplateId").value(templateId))
                .andExpect(jsonPath("$[0].locationId").value(locationId))
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].endTime").exists());
    }

    @Test
    @DisplayName("Admin can assign employee to shift and retrieve assignments by shift and by employee")
    void adminCanAssignEmployeeToShiftAndRetrieveAssignments() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "assign");
        Long locationId = createLocation(adminToken, companyId, "assign");
        Long positionId = createPosition(adminToken, companyId);
        Long employeeId = createEmployee(adminToken, companyId, positionId, locationId, "assign");

        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                startTime,
                endTime,
                "Assign shift",
                null,
                null,
                true,
                null
        );
        mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated());

        int year = 2026;
        int month = 7;
        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, year, month);
        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated());

        LocalDate shiftDate = LocalDate.of(year, month, 15);
        MvcResult listResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}"
                        , locationId, shiftDate)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> instances = objectMapper.readValue(
                listResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(instances).isNotEmpty();
        Long shiftInstanceId = instances.get(0).id();

        ShiftAssignmentRequestDto assignRequest = new ShiftAssignmentRequestDto(shiftInstanceId, employeeId);
        MvcResult assignResult = mockMvc.perform(post("/api/shift-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.shiftInstanceId").value(shiftInstanceId))
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andReturn();

        ShiftAssignmentResponseDto assignment = objectMapper.readValue(
                assignResult.getResponse().getContentAsString(),
                ShiftAssignmentResponseDto.class
        );
        assertThat(assignment.id()).isNotNull();

        mockMvc.perform(get("/api/shift-assignments/shift-instance/{shiftInstanceId}", shiftInstanceId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(employeeId));

        mockMvc.perform(get("/api/shift-assignments/employee/{employeeId}", employeeId)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shiftInstanceId").value(shiftInstanceId));
    }

    @Test
    @DisplayName("Assigning same employee twice to same shift returns 400")
    void assigningSameEmployeeTwiceToSameShiftReturns400() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "dup");
        Long locationId = createLocation(adminToken, companyId, "dup");
        Long positionId = createPosition(adminToken, companyId);
        Long employeeId = createEmployee(adminToken, companyId, positionId, locationId, "dup");

        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 2, null)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Dup shift",
                null,
                null,
                true,
                null
        );
        mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated());

        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, 2026, 8);
        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated());

        MvcResult listResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}", locationId, "2026-08-10")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> instances = objectMapper.readValue(
                listResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(instances).isNotEmpty();
        Long shiftInstanceId = instances.get(0).id();

        ShiftAssignmentRequestDto assignRequest = new ShiftAssignmentRequestDto(shiftInstanceId, employeeId);
        mockMvc.perform(post("/api/shift-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isCreated());

        MvcResult badResult = mockMvc.perform(post("/api/shift-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(badResult.getResponse().getContentAsString()).contains("already assigned");
    }

    @Test
    @DisplayName("Assigning employee with blocking availability on shift date returns 400")
    void assigningEmployeeWithBlockingAvailabilityReturns400() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "block");
        Long locationId = createLocation(adminToken, companyId, "block");
        Long positionId = createPosition(adminToken, companyId);
        Long employeeId = createEmployee(adminToken, companyId, positionId, locationId, "block");

        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Block shift",
                null,
                null,
                true,
                null
        );
        mockMvc.perform(post("/api/shift-templates")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(templateRequest)))
                .andExpect(status().isCreated());

        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, 2026, 9);
        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated());

        LocalDate shiftDate = LocalDate.of(2026, 9, 20);
        AvailabilityRequestDto blockingAvailability = new AvailabilityRequestDto(
                employeeId,
                shiftDate,
                shiftDate,
                AvailabilityType.UNAVAILABLE
        );
        mockMvc.perform(post("/api/availability")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blockingAvailability)))
                .andExpect(status().isCreated());

        MvcResult listResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}", locationId, shiftDate)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> instances = objectMapper.readValue(
                listResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(instances).isNotEmpty();
        Long shiftInstanceId = instances.get(0).id();

        ShiftAssignmentRequestDto assignRequest = new ShiftAssignmentRequestDto(shiftInstanceId, employeeId);
        MvcResult badResult = mockMvc.perform(post("/api/shift-assignments")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(badResult.getResponse().getContentAsString()).contains("marked as");
    }
}
