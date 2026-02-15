package com.shifterizator.shifterizatorbackend.user.service;

import com.shifterizator.shifterizatorbackend.user.dto.AssignableUserDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface UserService {

    User createUser(UserRequestDto requestDto);

    User updateUser(Long id, UserRequestDto requestDto);

    User deactivateUser(Long id);

    User activateUser(Long id);

    void deleteUser(Long id);

    void deleteUser(Long id, boolean physicalDelete);

    User getUser(Long id);

    Page<User> search(String role, Long companyId, String username, String email, Boolean isActive, Pageable pageable);

    List<User> listByCompany(Long companyId);

    List<AssignableUserDto> listAssignableByCompanyIds(Set<Long> companyIds);

    User resetPassword(Long id, String newPassword);

    User updateProfilePicture(Long userId, String profilePictureUrl);
}
