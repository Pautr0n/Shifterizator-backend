package com.shifterizator.shifterizatorbackend.user.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntity_should_map_all_fields() {
        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                5L,
                null
        );

        Company company = new Company();
        company.setId(5L);

        User user = mapper.toEntity(dto, "hashedPass", company);

        assertThat(dto.companyId()).isEqualTo(5L);
        assertThat(user.getUsername()).isEqualTo("john");
        assertThat(user.getEmail()).isEqualTo("john@mail.com");
        assertThat(user.getPassword()).isEqualTo("hashedPass");
        assertThat(user.getRole()).isEqualTo(Role.EMPLOYEE);
        assertThat(user.getCompany()).isEqualTo(company);
    }

    @Test
    void toDto_should_map_all_fields() {
        Company company = new Company();
        company.setId(5L);

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, company);
        user.setId(10L);
        user.setIsActive(true);

        UserResponseDto dto = mapper.toDto(user);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.email()).isEqualTo("john@mail.com");
        assertThat(dto.phone()).isNull();
        assertThat(dto.role()).isEqualTo("EMPLOYEE");
        assertThat(dto.companyId()).isEqualTo(5L);
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void toDto_should_set_companyId_null_when_company_null() {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);

        UserResponseDto dto = mapper.toDto(user);

        assertThat(dto.companyId()).isNull();
    }

}