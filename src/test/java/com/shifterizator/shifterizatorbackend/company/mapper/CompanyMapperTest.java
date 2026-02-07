package com.shifterizator.shifterizatorbackend.company.mapper;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CompanyMapperTest {

    CompanyMapper companyMapper = new CompanyMapper();

    LocalDateTime createdAt = LocalDateTime.of(2024
            , 1
            , 1
            , 0
            , 0);

    LocalDateTime updatedAt = LocalDateTime.of(2024
            , 1
            , 1
            , 1
            , 0);


    @Test
    void toDto_successfully_converts_entity_to_dto() {

        Company company = new Company("Name"
                , "Legal name"
                , "4444444N"
                , "test@test.com"
                , "+34932231515");

        company.setId(1236456L);
        company.setIsActive(true);
        company.setCreatedAt(createdAt);
        company.setUpdatedAt(updatedAt);
        company.setCountry("ES");

        CompanyResponseDto responseDto = companyMapper.toDto(company);

        assertThat(responseDto).extracting("id"
                        , "name"
                        , "legalName"
                        , "taxId"
                        , "email"
                        , "phone"
                        , "country"
                        , "isActive"
                        , "createdAt"
                        , "updatedAt"
                        , "createdBy"
                        , "updatedBy")
                .containsExactly(company.getId()
                        , company.getName()
                        , company.getLegalName()
                        , company.getTaxId()
                        , company.getEmail()
                        , company.getPhone()
                        , company.getCountry()
                        , company.getIsActive()
                        , company.getCreatedAt()
                        , company.getUpdatedAt()
                        , company.getCreatedBy()
                        , company.getUpdatedBy());


    }

    @Test
    void toEntity_successfully_converts_dto_to_entity() {
        CompanyRequestDto requestDto = new CompanyRequestDto("Test Name"
                , "Test Legal Name"
                , "12345678N"
                , "test@test.com"
                , "+34123456789"
                , "ES");

        Company company = companyMapper.toEntity(requestDto);

        assertThat(company).extracting("id"
                        , "name"
                        , "legalName"
                        , "taxId"
                        , "email"
                        , "phone"
                        , "country"
                        , "isActive"
                        , "createdAt"
                        , "updatedAt")
                .containsExactly(null
                        , requestDto.name()
                        , requestDto.legalName()
                        , requestDto.taxId()
                        , requestDto.email()
                        , requestDto.phone()
                        , requestDto.country()
                        , true
                        , null
                        , null);

    }
}