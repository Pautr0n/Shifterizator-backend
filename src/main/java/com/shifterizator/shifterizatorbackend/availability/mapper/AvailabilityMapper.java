package com.shifterizator.shifterizatorbackend.availability.mapper;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityMapper {

    public AvailabilityResponseDto toDto(EmployeeAvailability availability) {
        String employeeName = availability.getEmployee() != null
                ? availability.getEmployee().getName() + " " + availability.getEmployee().getSurname()
                : null;
        return new AvailabilityResponseDto(
                availability.getId(),
                availability.getEmployee() != null ? availability.getEmployee().getId() : null,
                employeeName,
                availability.getStartDate(),
                availability.getEndDate(),
                availability.getType(),
                availability.getCreatedAt(),
                availability.getUpdatedAt(),
                availability.getCreatedBy(),
                availability.getUpdatedBy()
        );
    }

    public EmployeeAvailability toEntity(AvailabilityRequestDto dto, com.shifterizator.shifterizatorbackend.employee.model.Employee employee) {
        return EmployeeAvailability.builder()
                .employee(employee)
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .type(dto.type())
                .build();
    }
}
