package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplateLanguageRequirement;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftTemplateMapperTest {

    private final ShiftTemplateMapper mapper = new ShiftTemplateMapper();

    @Test
    void toDto_shouldMapEntityToDto() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Position position1 = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Position position2 = Position.builder().id(2L).name("Manager").company(company).build();
        Language language = Language.builder().id(1L).code("EN").name("English").build();

        ShiftTemplate template = ShiftTemplate.builder()
                .id(99L)
                .location(location)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .description("Morning shift")
                .isActive(true)
                .priority(1)
                .build();

        ShiftTemplatePosition stp1 = ShiftTemplatePosition.builder()
                .shiftTemplate(template)
                .position(position1)
                .requiredCount(2)
                .build();
        ShiftTemplatePosition stp2 = ShiftTemplatePosition.builder()
                .shiftTemplate(template)
                .position(position2)
                .requiredCount(1)
                .build();

        template.setRequiredPositions(Set.of(stp1, stp2));
        ShiftTemplateLanguageRequirement langReq = ShiftTemplateLanguageRequirement.builder()
                .shiftTemplate(template)
                .language(language)
                .requiredCount(1)
                .build();
        template.setRequiredLanguageRequirements(Set.of(langReq));

        var dto = mapper.toDto(template);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.locationId()).isEqualTo(1L);
        assertThat(dto.locationName()).isEqualTo("HQ");
        assertThat(dto.requiredPositions()).hasSize(2);
        assertThat(dto.totalRequiredEmployees()).isEqualTo(3);
        assertThat(dto.requiredLanguages()).contains("English");
        assertThat(dto.requiredLanguageRequirements()).hasSize(1);
        assertThat(dto.requiredLanguageRequirements().get(0).languageName()).isEqualTo("English");
        assertThat(dto.requiredLanguageRequirements().get(0).requiredCount()).isEqualTo(1);
        assertThat(dto.priority()).isEqualTo(1);
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();

        ShiftTemplateRequestDto dto = new ShiftTemplateRequestDto(
                1L,
                List.of(new PositionRequirementDto(1L, 1, null)),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Morning shift",
                Set.of(),
                null,
                null,
                true,
                2
        );

        ShiftTemplate entity = mapper.toEntity(dto, location);

        assertThat(entity.getLocation()).isEqualTo(location);
        assertThat(entity.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(entity.getEndTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(entity.getDescription()).isEqualTo("Morning shift");
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getPriority()).isEqualTo(2);
    }
}
