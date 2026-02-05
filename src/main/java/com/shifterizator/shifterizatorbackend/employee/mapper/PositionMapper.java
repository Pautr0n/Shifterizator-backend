package com.shifterizator.shifterizatorbackend.employee.mapper;

import com.shifterizator.shifterizatorbackend.employee.dto.PositionDto;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionDto toDto(Position position) {
        return new PositionDto(
                position.getId(),
                position.getName(),
                position.getCompany().getId()
        );
    }
}
