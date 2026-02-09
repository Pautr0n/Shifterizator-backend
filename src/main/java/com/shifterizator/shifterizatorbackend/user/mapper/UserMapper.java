package com.shifterizator.shifterizatorbackend.user.mapper;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDto dto, String hashedPassword, Company company) {
        return User.builder()
                .username(dto.username())
                .email(dto.email())
                .password(hashedPassword)
                .role(Role.valueOf(dto.role()))
                .company(company)
                .phone(dto.phone())
                .build();
    }

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getCompany() != null ? user.getCompany().getId() : null,
                user.getIsActive(),
                user.getCreatedBy(),
                user.getUpdatedBy()
        );
    }
}
