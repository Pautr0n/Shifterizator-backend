package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ShiftTemplateMapper {

    public ShiftTemplateResponseDto toDto(ShiftTemplate template) {
        String locationName = template.getLocation() != null ? template.getLocation().getName() : null;
        
        List<PositionRequirementResponseDto> requiredPositions = template.getRequiredPositions() != null
                ? template.getRequiredPositions().stream()
                .map(stp -> new PositionRequirementResponseDto(
                        stp.getPosition().getId(),
                        stp.getPosition().getName(),
                        stp.getRequiredCount(),
                        stp.getIdealCount()
                ))
                .collect(Collectors.toList())
                : List.of();

        Integer totalRequiredEmployees = template.getRequiredPositions() != null
                ? template.getRequiredPositions().stream()
                .mapToInt(ShiftTemplatePosition::getRequiredCount)
                .sum()
                : 0;

        Set<String> languages = template.getRequiredLanguages() != null
                ? template.getRequiredLanguages().stream()
                .map(Language::getName)
                .collect(Collectors.toSet())
                : Set.of();

        return new ShiftTemplateResponseDto(
                template.getId(),
                template.getLocation() != null ? template.getLocation().getId() : null,
                locationName,
                requiredPositions,
                template.getStartTime(),
                template.getEndTime(),
                totalRequiredEmployees,
                template.getIdealEmployees(),
                template.getDescription(),
                languages,
                template.getIsActive(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getCreatedBy(),
                template.getUpdatedBy()
        );
    }

    public ShiftTemplate toEntity(ShiftTemplateRequestDto dto, Location location, Set<Language> languages) {
        return ShiftTemplate.builder()
                .location(location)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .description(dto.description())
                .idealEmployees(dto.idealEmployees())
                .requiredLanguages(languages != null ? languages : Set.of())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();
    }
}
