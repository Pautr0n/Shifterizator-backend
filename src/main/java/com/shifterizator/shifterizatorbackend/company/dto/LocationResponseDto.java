package com.shifterizator.shifterizatorbackend.company.dto;

import java.util.Set;public record LocationResponseDto(
        Long id,
        String name,
        String address,
        Long companyId,
        Set<String> openDaysOfWeek

) {
}
