package com.shifterizator.shifterizatorbackend.availability.mapper;

import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityRequestDto;
import com.shifterizator.shifterizatorbackend.availability.dto.AvailabilityResponseDto;
import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityMapperTest {

    private final AvailabilityMapper mapper = new AvailabilityMapper();

    @Test
    void toDto_shouldMapAllFields() {
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").build();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 12, 0);
        EmployeeAvailability availability = EmployeeAvailability.builder()
                .id(99L)
                .employee(employee)
                .startDate(LocalDate.of(2024, 6, 1))
                .endDate(LocalDate.of(2024, 6, 15))
                .type(AvailabilityType.VACATION)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .createdBy("admin")
                .updatedBy("admin")
                .build();

        AvailabilityResponseDto dto = mapper.toDto(availability);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.employeeId()).isEqualTo(1L);
        assertThat(dto.employeeName()).isEqualTo("John Doe");
        assertThat(dto.startDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(dto.endDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(dto.type()).isEqualTo(AvailabilityType.VACATION);
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        assertThat(dto.createdBy()).isEqualTo("admin");
        assertThat(dto.updatedBy()).isEqualTo("admin");
    }

    @Test
    void toDto_shouldHandleNullEmployee() {
        EmployeeAvailability availability = EmployeeAvailability.builder()
                .id(1L)
                .employee(null)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 2))
                .type(AvailabilityType.AVAILABLE)
                .build();

        AvailabilityResponseDto dto = mapper.toDto(availability);

        assertThat(dto.employeeId()).isNull();
        assertThat(dto.employeeName()).isNull();
    }

    @Test
    void toEntity_shouldMapFromDtoAndEmployee() {
        AvailabilityRequestDto dto = new AvailabilityRequestDto(
                1L,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 15),
                AvailabilityType.SICK_LEAVE
        );
        Employee employee = Employee.builder().id(1L).name("Jane").surname("Doe").build();

        EmployeeAvailability entity = mapper.toEntity(dto, employee);

        assertThat(entity.getEmployee()).isSameAs(employee);
        assertThat(entity.getStartDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(entity.getEndDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(entity.getType()).isEqualTo(AvailabilityType.SICK_LEAVE);
        assertThat(entity.getId()).isNull();
    }
}
