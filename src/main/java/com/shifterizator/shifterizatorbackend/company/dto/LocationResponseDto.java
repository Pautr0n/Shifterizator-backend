package com.shifterizator.shifterizatorbackend.company.dto;

public record LocationResponseDto(
        Long id,
        String name,
        String address,
        Long companyId

) {
}
