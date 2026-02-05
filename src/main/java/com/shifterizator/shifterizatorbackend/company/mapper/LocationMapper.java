package com.shifterizator.shifterizatorbackend.company.mapper;

import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {


    public LocationResponseDto toDto(Location location) {
        return new LocationResponseDto(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCompany().getId()
        );
    }
}
