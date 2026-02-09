package com.shifterizator.shifterizatorbackend.blackoutdays.mapper;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BlackoutDayMapperTest {

    private final BlackoutDayMapper mapper = new BlackoutDayMapper();

    @Test
    void toDto_shouldMapEntityToDto() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(location)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Holiday closure")
                .appliesToCompany(false)
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        BlackoutDayResponseDto dto = mapper.toDto(blackoutDay);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.locationId()).isEqualTo(1L);
        assertThat(dto.locationName()).isEqualTo("HQ");
        assertThat(dto.date()).isEqualTo(LocalDate.of(2024, 12, 24));
        assertThat(dto.reason()).isEqualTo("Holiday closure");
        assertThat(dto.appliesToCompany()).isFalse();
        assertThat(dto.createdBy()).isEqualTo("admin");
        assertThat(dto.updatedBy()).isEqualTo("admin");
    }

    @Test
    void toDto_shouldHandleNullLocation() {
        BlackoutDay blackoutDay = BlackoutDay.builder()
                .id(99L)
                .location(null)
                .date(LocalDate.of(2024, 12, 24))
                .reason("Holiday closure")
                .appliesToCompany(false)
                .build();

        BlackoutDayResponseDto dto = mapper.toDto(blackoutDay);

        assertThat(dto.locationId()).isNull();
        assertThat(dto.locationName()).isNull();
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                LocalDate.of(2024, 12, 24),
                "Holiday closure",
                false
        );

        BlackoutDay entity = mapper.toEntity(dto, location);

        assertThat(entity.getLocation()).isEqualTo(location);
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2024, 12, 24));
        assertThat(entity.getReason()).isEqualTo("Holiday closure");
        assertThat(entity.getAppliesToCompany()).isFalse();
    }

    @Test
    void toEntity_shouldDefaultAppliesToCompanyToFalseWhenNull() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                LocalDate.of(2024, 12, 24),
                "Holiday closure",
                null
        );

        BlackoutDay entity = mapper.toEntity(dto, location);

        assertThat(entity.getAppliesToCompany()).isFalse();
    }

    @Test
    void toEntity_shouldSetAppliesToCompanyToTrue() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        BlackoutDayRequestDto dto = new BlackoutDayRequestDto(
                1L,
                LocalDate.of(2024, 12, 24),
                "Holiday closure",
                true
        );

        BlackoutDay entity = mapper.toEntity(dto, location);

        assertThat(entity.getAppliesToCompany()).isTrue();
    }
}
