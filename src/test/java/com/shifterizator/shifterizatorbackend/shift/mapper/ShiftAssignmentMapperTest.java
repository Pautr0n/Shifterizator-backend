package com.shifterizator.shifterizatorbackend.shift.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftAssignmentMapperTest {

    private final ShiftAssignmentMapper mapper = new ShiftAssignmentMapper();

    @Test
    void toDto_shouldMapEntityToDto() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        Position position = Position.builder().id(1L).name("Sales Assistant").company(company).build();
        Employee employee = Employee.builder().id(1L).name("John").surname("Doe").position(position).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();
        ShiftInstance instance = ShiftInstance.builder().id(99L).shiftTemplate(template).location(location).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(instance)
                .employee(employee)
                .isConfirmed(true)
                .assignedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .assignedBy("admin")
                .confirmedAt(LocalDateTime.of(2024, 1, 2, 10, 0))
                .build();

        var dto = mapper.toDto(assignment);

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.shiftInstanceId()).isEqualTo(99L);
        assertThat(dto.employeeId()).isEqualTo(1L);
        assertThat(dto.employeeName()).isEqualTo("John Doe");
        assertThat(dto.isConfirmed()).isTrue();
        assertThat(dto.assignedBy()).isEqualTo("admin");
    }

    @Test
    void toDto_shouldHandleNullEmployee() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        Location location = Location.builder().id(1L).name("HQ").address("Main").company(company).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(location).build();
        ShiftInstance instance = ShiftInstance.builder().id(99L).shiftTemplate(template).location(location).build();

        ShiftAssignment assignment = ShiftAssignment.builder()
                .id(100L)
                .shiftInstance(instance)
                .employee(null)
                .isConfirmed(false)
                .build();

        var dto = mapper.toDto(assignment);

        assertThat(dto.employeeId()).isNull();
        assertThat(dto.employeeName()).isNull();
    }
}
