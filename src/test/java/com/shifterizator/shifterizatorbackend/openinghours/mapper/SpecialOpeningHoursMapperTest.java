package com.shifterizator.shifterizatorbackend.openinghours.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialOpeningHoursMapperTest {

    private final SpecialOpeningHoursMapper mapper = new SpecialOpeningHoursMapper();

    @Test
    void toDto_shouldMapAllFields() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder()
                .id(10L)
                .name("HQ")
                .address("Main Street")
                .company(company)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 12, 0);

        SpecialOpeningHours openingHours = SpecialOpeningHours.builder()
                .id(99L)
                .location(location)
                .date(LocalDate.of(2024, 12, 24))
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(18, 0))
                .reason("Christmas Eve")
                .colorCode("#FF0000")
                .appliesToCompany(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        SpecialOpeningHoursResponseDto dto = mapper.toDto(openingHours);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.locationId()).isEqualTo(10L);
        assertThat(dto.locationName()).isEqualTo("HQ");
        assertThat(dto.date()).isEqualTo(LocalDate.of(2024, 12, 24));
        assertThat(dto.openTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(dto.closeTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(dto.reason()).isEqualTo("Christmas Eve");
        assertThat(dto.colorCode()).isEqualTo("#FF0000");
        assertThat(dto.appliesToCompany()).isFalse();
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        assertThat(dto.createdBy()).isEqualTo("admin");
        assertThat(dto.updatedBy()).isEqualTo("admin");
    }

    @Test
    void toEntity_shouldMapFromDtoAndLocation() {
        SpecialOpeningHoursRequestDto dto = new SpecialOpeningHoursRequestDto(
                10L,
                LocalDate.of(2024, 12, 31),
                LocalTime.of(10, 0),
                LocalTime.of(16, 0),
                "New Year's Eve",
                "#00FF00",
                true
        );

        Location location = Location.builder()
                .id(10L)
                .name("HQ")
                .address("Main Street")
                .build();

        SpecialOpeningHours entity = mapper.toEntity(dto, location);

        assertThat(entity.getLocation()).isSameAs(location);
        assertThat(entity.getDate()).isEqualTo(dto.date());
        assertThat(entity.getOpenTime()).isEqualTo(dto.openTime());
        assertThat(entity.getCloseTime()).isEqualTo(dto.closeTime());
        assertThat(entity.getReason()).isEqualTo(dto.reason());
        assertThat(entity.getColorCode()).isEqualTo(dto.colorCode());
        assertThat(entity.getAppliesToCompany()).isTrue();
    }
}

