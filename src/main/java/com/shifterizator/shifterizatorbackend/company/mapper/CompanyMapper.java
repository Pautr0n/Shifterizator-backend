package com.shifterizator.shifterizatorbackend.company.mapper;

import com.shifterizator.shifterizatorbackend.company.dto.CompanyRequestDto;
import com.shifterizator.shifterizatorbackend.company.dto.CompanyResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public CompanyResponseDto toDto (Company company){
        CompanyResponseDto responseDto = new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getLegalName(),
                company.getTaxId(),
                company.getEmail(),
                company.getPhone(),
                company.getIsActive(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );

        return responseDto;
    }

    public Company toEntity (CompanyRequestDto requestDto){
        Company company = new Company();
        company.setName(requestDto.name());
        company.setLegalName(requestDto.legalName());
        company.setTaxId(requestDto.taxId());
        company.setEmail(requestDto.email());
        company.setPhone(requestDto.phone());

        return company;
    }


}
