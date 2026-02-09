package com.shifterizator.shifterizatorbackend.shift.dto;

public record PositionRequirementResponseDto(
        Long positionId,
        String positionName,
        Integer requiredCount
) {
}
