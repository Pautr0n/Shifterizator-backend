package com.shifterizator.shifterizatorbackend.company.mapper;

import com.shifterizator.shifterizatorbackend.company.dto.LocationResponseDto;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.stereotype.Component;import java.util.Set;import java.util.stream.Collectors;

@Component
public class LocationMapper {


    public LocationResponseDto toDto(Location location) {
        return new LocationResponseDto(
                location.getId(),
                location.getName(),
                location.getAddress(),
                location.getCompany().getId(),
                getOpenDays(location)
        );
    }

    public static Set<java.time.DayOfWeek> toOpenDaysOfWeek(Set<String> openDaysOfWeek) {
        if (openDaysOfWeek == null || openDaysOfWeek.isEmpty()) {
            return null;
        }
        return openDaysOfWeek.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(java.time.DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }

    private Set<String> getOpenDays (Location location){
        Set<String> openDays = location.getOpenDaysOfWeek() == null
                ? null
                : location.getOpenDaysOfWeek().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return openDays;
    }
}
