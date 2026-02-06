package com.shifterizator.shifterizatorbackend.employee.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionMapperTest {

    private final PositionMapper mapper = new PositionMapper();

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        Company company = new Company();
                company.setId(3L);
                company.setName("Skynet");

        Position position = Position.builder()
                .id(1L)
                .name("Manager")
                .company(company)
                .build();

        PositionDto dto = mapper.toDto(position);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Manager");
        assertThat(dto.companyId()).isEqualTo(3L);
    }
}
