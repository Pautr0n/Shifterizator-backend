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
        User user = new User(
                dto.username(),
                dto.email(),
                hashedPassword,
                Role.valueOf(dto.role()),
                company
        );
        user.setPhone(dto.phone());
        return user;
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
