package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftInstanceMapperTest {

    private final ShiftInstanceMapper mapper = new ShiftInstanceMapper();

    @Test
    void toDto_shouldMapEntityToDto() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstance instance = ShiftInstance.builder()
                .id(99L)
                .shiftTemplate(template)
                .location(location)
                .date(LocalDate.of(2024, 12, 24))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(3)
                .notes("Test notes")
                .isComplete(false)
                .build();

        var dto = mapper.toDto(instance, 2, List.of(), List.of());

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.shiftTemplateId()).isEqualTo(1L);
        assertThat(dto.locationId()).isEqualTo(1L);
        assertThat(dto.locationName()).isEqualTo("HQ");
        assertThat(dto.date()).isEqualTo(LocalDate.of(2024, 12, 24));
        assertThat(dto.assignedEmployees()).isEqualTo(2);
        assertThat(dto.isComplete()).isFalse();
        assertThat(dto.positionRequirementStatus()).isEmpty();
        assertThat(dto.languageRequirementStatus()).isEmpty();
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();

        ShiftInstanceRequestDto dto = new ShiftInstanceRequestDto(
                1L,
                1L,
                LocalDate.of(2024, 12, 24),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                3,
                null,
                "Test notes"
        );

        ShiftInstance entity = mapper.toEntity(dto, template, location);

        assertThat(entity.getShiftTemplate()).isEqualTo(template);
        assertThat(entity.getLocation()).isEqualTo(location);
        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2024, 12, 24));
        assertThat(entity.getRequiredEmployees()).isEqualTo(3);
        assertThat(entity.getIsComplete()).isFalse();
    }
}
