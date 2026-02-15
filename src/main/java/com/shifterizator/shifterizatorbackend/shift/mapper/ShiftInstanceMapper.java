package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.shift.dto.LanguageRequirementStatusDto;
import com.shifterizator.shifterizatorbackend.shift.dto.PositionRequirementStatusDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceRequestDto;
import com.shifterizator.shifterizatorbackend.shift.dto.ShiftInstanceResponseDto;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShiftInstanceMapper {

    public ShiftInstanceResponseDto toDto(ShiftInstance instance, int assignedEmployees,
                                          List<PositionRequirementStatusDto> positionRequirementStatus,
                                          List<LanguageRequirementStatusDto> languageRequirementStatus) {
        String locationName = instance.getLocation() != null ? instance.getLocation().getName() : null;
        List<PositionRequirementStatusDto> posStatus = positionRequirementStatus != null ? positionRequirementStatus : List.of();
        List<LanguageRequirementStatusDto> langStatus = languageRequirementStatus != null ? languageRequirementStatus : List.of();
        return new ShiftInstanceResponseDto(
                instance.getId(),
                instance.getShiftTemplate() != null ? instance.getShiftTemplate().getId() : null,
                instance.getLocation() != null ? instance.getLocation().getId() : null,
                locationName,
                instance.getDate(),
                instance.getStartTime(),
                instance.getEndTime(),
                instance.getRequiredEmployees(),
                instance.getIdealEmployees(),
                assignedEmployees,
                instance.getIsComplete(),
                posStatus,
                langStatus,
                instance.getNotes(),
                instance.getCreatedAt(),
                instance.getUpdatedAt(),
                instance.getCreatedBy(),
                instance.getUpdatedBy()
        );
    }

    public ShiftInstance toEntity(ShiftInstanceRequestDto dto, ShiftTemplate template, Location location) {
        return ShiftInstance.builder()
                .shiftTemplate(template)
                .location(location)
                .date(dto.date())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .requiredEmployees(dto.requiredEmployees() != null ? dto.requiredEmployees() : 1)
                .idealEmployees(dto.idealEmployees())
                .notes(dto.notes())
                .isComplete(false)
                .build();
    }
}
