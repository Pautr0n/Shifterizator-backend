package com.shifterizator.shifterizatorbackend.blackoutdays.mapper;

import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayRequestDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.dto.BlackoutDayResponseDto;
import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.stereotype.Component;

@Component
public class BlackoutDayMapper {

    public BlackoutDayResponseDto toDto(BlackoutDay blackoutDay) {
        String locationName = blackoutDay.getLocation() != null ? blackoutDay.getLocation().getName() : null;
        return new BlackoutDayResponseDto(
                blackoutDay.getId(),
                blackoutDay.getLocation() != null ? blackoutDay.getLocation().getId() : null,
                locationName,
                blackoutDay.getDate(),
                blackoutDay.getReason(),
                blackoutDay.getAppliesToCompany(),
                blackoutDay.getCreatedAt(),
                blackoutDay.getUpdatedAt(),
                blackoutDay.getCreatedBy(),
                blackoutDay.getUpdatedBy()
        );
    }

    public BlackoutDay toEntity(BlackoutDayRequestDto dto, Location location) {
        return BlackoutDay.builder()
                .location(location)
                .date(dto.date())
                .reason(dto.reason())
                .appliesToCompany(dto.appliesToCompany() != null ? dto.appliesToCompany() : false)
                .build();
    }
}
