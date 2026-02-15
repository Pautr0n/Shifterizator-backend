package com.shifterizator.shifterizatorbackend.employee.mapper;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeResponseDto;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    private final EmployeeMapper mapper = new EmployeeMapper();

    @Test
    void toEntity_shouldMapBasicFieldsAndPosition() {
        Position position = Position.builder()
                .id(5L)
                .name("Waiter")
                .build();

        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John",
                "Connor",
                "john@example.com",
                "123456789",
                5L,
                Set.of(1L),
                Set.of(2L),
                Set.of(1L),
                null,
                5,
                null,
                null,
                null
        );

        Employee employee = mapper.toEntity(dto, position);

        assertThat(employee.getName()).isEqualTo("John");
        assertThat(employee.getSurname()).isEqualTo("Connor");
        assertThat(employee.getEmail()).isEqualTo("john@example.com");
        assertThat(employee.getPhone()).isEqualTo("123456789");
        assertThat(employee.getPosition()).isSameAs(position);
        assertThat(employee.getPreferredDayOff()).isNull();
    }

    @Test
    void toEntity_shouldMapPreferredDayOffWhenProvided() {
        Position position = Position.builder().id(5L).name("Waiter").build();
        EmployeeRequestDto dto = new EmployeeRequestDto(
                "John", "Connor", "john@example.com", "123",
                5L, Set.of(1L), Set.of(2L), Set.of(1L), "WEDNESDAY", 5, null, null, null
        );

        Employee employee = mapper.toEntity(dto, position);

        assertThat(employee.getPreferredDayOff()).isEqualTo(java.time.DayOfWeek.WEDNESDAY);
    }

    @Test
    void toDto_shouldMapAllFieldsAndCollections() {
        Company company1 = new Company();
        Company company2 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");
        company2.setId(2L);
        company2.setName("Cyberdyne");

        Location location1 = Location.builder().id(10L).name("HQ").build();
        Location location2 = Location.builder().id(11L).name("Branch").build();

        Position position = Position.builder().id(5L).name("Waiter").build();

        Employee employee = Employee.builder()
                .id(99L)
                .name("John")
                .surname("Connor")
                .email("john@example.com")
                .phone("123456789")
                .position(position)
                .shiftsPerWeek(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmployeeCompany ec1 = EmployeeCompany.builder().employee(employee).company(company1).build();
        EmployeeCompany ec2 = EmployeeCompany.builder().employee(employee).company(company2).build();
        employee.addCompany(ec1);
        employee.addCompany(ec2);

        EmployeeLocation el1 = EmployeeLocation.builder().employee(employee).location(location1).build();
        EmployeeLocation el2 = EmployeeLocation.builder().employee(employee).location(location2).build();
        employee.addLocation(el1);
        employee.addLocation(el2);

        Language lang1 = Language.builder().id(1L).code("EN").name("English").build();
        Language lang2 = Language.builder().id(2L).code("ES").name("Spanish").build();
        EmployeeLanguage eLang1 = EmployeeLanguage.builder().employee(employee).language(lang1).build();
        EmployeeLanguage eLang2 = EmployeeLanguage.builder().employee(employee).language(lang2).build();
        employee.addLanguage(eLang1);
        employee.addLanguage(eLang2);

        EmployeeResponseDto dto = mapper.toDto(employee);

        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.name()).isEqualTo("John");
        assertThat(dto.surname()).isEqualTo("Connor");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.phone()).isEqualTo("123456789");
        assertThat(dto.position()).isEqualTo("Waiter");
        assertThat(dto.companies()).containsExactlyInAnyOrder("Skynet", "Cyberdyne");
        assertThat(dto.locations()).containsExactlyInAnyOrder("HQ", "Branch");
        assertThat(dto.languages()).containsExactlyInAnyOrder("English", "Spanish");
        assertThat(dto.shiftsPerWeek()).isEqualTo(5);
        assertThat(dto.createdAt()).isNotNull();
        assertThat(dto.updatedAt()).isNotNull();
    }

    @Test
    void toDto_shouldMapEmptyLanguagesWhenNone() {
        Position position = Position.builder().id(5L).name("Waiter").build();
        Employee employee = Employee.builder()
                .id(99L)
                .name("John")
                .surname("Connor")
                .position(position)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        EmployeeResponseDto dto = mapper.toDto(employee);

        assertThat(dto.languages()).isEmpty();
    }
}
