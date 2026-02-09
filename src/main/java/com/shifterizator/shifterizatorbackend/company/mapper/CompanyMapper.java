package com.shifterizator.shifterizatorbackend.company.mapper;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyResponseDto toDto(Company company) {
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getLegalName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getCountry(),
                company.getIsActive(),
                company.getCreatedAt(),
                company.getUpdatedAt(),
                company.getCreatedBy(),
                company.getUpdatedBy()
        );
    }

    public Company toEntity(CompanyRequestDto requestDto) {
        return Company.builder()
                .name(requestDto.name())
                .legalName(requestDto.legalName())
                .taxId(requestDto.taxId())
                .email(requestDto.email())
                .phone(requestDto.phone())
                .country(requestDto.country())
                .isActive(true)
                .build();
    }


}
