package com.shifterizator.shifterizatorbackend.company.mapper;


import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private final LocationMapper mapper = new LocationMapper();

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        Company company = new Company();
                company.setId(10L);
                company.setName("Skynet");


        Location location = Location.builder()
                .id(1L)
                .name("HQ")
                .address("Main Street 1")
                .company(company)
                .build();

        LocationResponseDto dto = mapper.toDto(location);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("HQ");
        assertThat(dto.address()).isEqualTo("Main Street 1");
        assertThat(dto.companyId()).isEqualTo(10L);
    }
}
