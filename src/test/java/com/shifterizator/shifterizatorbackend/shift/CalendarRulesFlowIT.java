package com.shifterizator.shifterizatorbackend.shift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CalendarRulesFlowIT extends BaseIntegrationTest {

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
                "CalendarCo-" + suffix,
                "Calendar Company " + suffix + " S.A.",
                taxId,
                "calendco" + suffix + "@example.com",
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
                "CalLoc-" + suffix,
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

    @Test
    @DisplayName("Blackout days prevent shift generation, special opening hours override template times")
    void blackoutDaysPreventShiftsAndSpecialHoursOverrideTemplateTimes() throws Exception {
        String adminToken = loginAndGetBearerToken("admin", "Admin123!");
        Long companyId = createCompany(adminToken, "cal");
        Long locationId = createLocation(adminToken, companyId, "cal");
        Long positionId = createPosition(adminToken, companyId);

        LocalTime templateStartTime = LocalTime.of(9, 0);
        LocalTime templateEndTime = LocalTime.of(17, 0);
        ShiftTemplateRequestDto templateRequest = new ShiftTemplateRequestDto(
                locationId,
                List.of(new PositionRequirementDto(positionId, 1, null)),
                templateStartTime,
                templateEndTime,
                "Regular shift",
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
        int month = 10;
        LocalDate blackoutDate = LocalDate.of(year, month, 5);
        LocalDate specialHoursDate = LocalDate.of(year, month, 10);
        LocalDate normalDate = LocalDate.of(year, month, 15);

        BlackoutDayRequestDto blackoutRequest = new BlackoutDayRequestDto(
                locationId,
                blackoutDate,
                "Holiday",
                false
        );
        mockMvc.perform(post("/api/blackout-days")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blackoutRequest)))
                .andExpect(status().isCreated());

        LocalTime specialOpenTime = LocalTime.of(10, 0);
        LocalTime specialCloseTime = LocalTime.of(18, 0);
        SpecialOpeningHoursRequestDto specialHoursRequest = new SpecialOpeningHoursRequestDto(
                locationId,
                specialHoursDate,
                specialOpenTime,
                specialCloseTime,
                "Extended hours",
                null,
                false
        );
        mockMvc.perform(post("/api/opening-hours")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialHoursRequest)))
                .andExpect(status().isCreated());

        GenerateMonthRequestDto generateRequest = new GenerateMonthRequestDto(locationId, year, month);
        mockMvc.perform(post("/api/shift-instances/generate-month")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated());

        MvcResult blackoutResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}", locationId, blackoutDate)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> blackoutShifts = objectMapper.readValue(
                blackoutResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(blackoutShifts).isEmpty();

        MvcResult specialResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}", locationId, specialHoursDate)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> specialShifts = objectMapper.readValue(
                specialResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(specialShifts).hasSize(1);
        assertThat(specialShifts.get(0).startTime()).isEqualTo(specialOpenTime);
        assertThat(specialShifts.get(0).endTime()).isEqualTo(specialCloseTime);

        MvcResult normalResult = mockMvc.perform(get("/api/shift-instances/by-location/{locationId}/date/{date}", locationId, normalDate)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<ShiftInstanceResponseDto> normalShifts = objectMapper.readValue(
                normalResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ShiftInstanceResponseDto.class)
        );
        assertThat(normalShifts).hasSize(1);
        assertThat(normalShifts.get(0).startTime()).isEqualTo(templateStartTime);
        assertThat(normalShifts.get(0).endTime()).isEqualTo(templateEndTime);
    }
}
