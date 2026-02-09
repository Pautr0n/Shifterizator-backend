package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ShiftTemplateMapper {

    public ShiftTemplateResponseDto toDto(ShiftTemplate template) {
        String locationName = template.getLocation() != null ? template.getLocation().getName() : null;
        String positionName = template.getPosition() != null ? template.getPosition().getName() : null;
        Set<String> languages = template.getRequiredLanguages() != null
                ? template.getRequiredLanguages().stream()
                .map(Language::getName)
                .collect(Collectors.toSet())
                : Set.of();

        return new ShiftTemplateResponseDto(
                template.getId(),
                template.getLocation() != null ? template.getLocation().getId() : null,
                locationName,
                template.getPosition() != null ? template.getPosition().getId() : null,
                positionName,
                template.getStartTime(),
                template.getEndTime(),
                template.getRequiredEmployees(),
                template.getDescription(),
                languages,
                template.getIsActive(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getCreatedBy(),
                template.getUpdatedBy()
        );
    }

    public ShiftTemplate toEntity(ShiftTemplateRequestDto dto, Location location, Position position, Set<Language> languages) {
        return ShiftTemplate.builder()
                .location(location)
                .position(position)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .requiredEmployees(dto.requiredEmployees() != null ? dto.requiredEmployees() : 1)
                .description(dto.description())
                .requiredLanguages(languages != null ? languages : Set.of())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();
    }
}
