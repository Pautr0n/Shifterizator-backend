package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.shift.dto.LanguageRequirementResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementResponseDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftTemplateResponseDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplateLanguageRequirement;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.company.model.Location;
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

        Integer totalIdealEmployees = template.getRequiredPositions() != null
                ? template.getRequiredPositions().stream()
                .mapToInt(stp -> stp.getIdealCount() != null ? stp.getIdealCount() : stp.getRequiredCount())
                .sum()
                : null;

        List<LanguageRequirementResponseDto> languageReqs = template.getRequiredLanguageRequirements() != null
                ? template.getRequiredLanguageRequirements().stream()
                .map(r -> new LanguageRequirementResponseDto(
                        r.getLanguage().getId(),
                        r.getLanguage().getName(),
                        r.getRequiredCount()))
                .collect(Collectors.toList())
                : List.of();

        Set<String> languageNames = template.getRequiredLanguageRequirements() != null
                ? template.getRequiredLanguageRequirements().stream()
                .map(r -> r.getLanguage().getName())
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
                totalIdealEmployees,
                template.getDescription(),
                languageNames,
                languageReqs,
                template.getIsActive(),
                template.getPriority(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getCreatedBy(),
                template.getUpdatedBy()
        );
    }

    public ShiftTemplate toEntity(ShiftTemplateRequestDto dto, Location location) {
        return ShiftTemplate.builder()
                .location(location)
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .description(dto.description())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .priority(dto.priority())
                .build();
    }
}
