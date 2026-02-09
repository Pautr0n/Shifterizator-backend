package com.shifterizator.shifterizatorbackend.openinghours.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursRequestDto;
import com.shifterizator.shifterizatorbackend.openinghours.dto.SpecialOpeningHoursResponseDto;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import org.springframework.stereotype.Component;

@Component
public class SpecialOpeningHoursMapper {

    public SpecialOpeningHoursResponseDto toDto(SpecialOpeningHours openingHours) {
        String locationName = openingHours.getLocation() != null ? openingHours.getLocation().getName() : null;
        return new SpecialOpeningHoursResponseDto(
                openingHours.getId(),
                openingHours.getLocation() != null ? openingHours.getLocation().getId() : null,
                locationName,
                openingHours.getDate(),
                openingHours.getOpenTime(),
                openingHours.getCloseTime(),
                openingHours.getReason(),
                openingHours.getColorCode(),
                openingHours.getAppliesToCompany(),
                openingHours.getCreatedAt(),
                openingHours.getUpdatedAt(),
                openingHours.getCreatedBy(),
                openingHours.getUpdatedBy()
        );
    }

    public SpecialOpeningHours toEntity(SpecialOpeningHoursRequestDto dto, Location location) {
        return SpecialOpeningHours.builder()
                .location(location)
                .date(dto.date())
                .openTime(dto.openTime())
                .closeTime(dto.closeTime())
                .reason(dto.reason())
                .colorCode(dto.colorCode())
                .appliesToCompany(dto.appliesToCompany() != null ? dto.appliesToCompany() : false)
                .build();
    }
}
