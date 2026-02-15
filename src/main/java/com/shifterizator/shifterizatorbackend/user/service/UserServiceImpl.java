package com.shifterizator.shifterizatorbackend.user.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.user.dto.AssignableUserDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.ForbiddenOperationException;
import com.shifterizator.shifterizatorbackend.user.exception.UserAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import com.shifterizator.shifterizatorbackend.user.spec.UserSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;


    @Override
    public User createUser(UserRequestDto requestDto) {
        validateUniqueUsername(requestDto.username());
        validateUniqueEmailOnCreate(requestDto.email());

        Company company = resolveCompanyForUser(requestDto);

        String hashedPassword = passwordEncoder.encode(requestDto.password());

        User newUser = userMapper.toEntity(requestDto, hashedPassword, company);

        return userRepository.save(newUser);
    }


    @Override
    @Transactional
    public User updateUser(Long id, UserRequestDto requestDto) {

        User user = validateUserExistsAndReturnUser(id);

        validateUpdateConstraints(requestDto, user);

        user.setUsername(requestDto.username());
        user.setEmail(requestDto.email());
        user.setPhone(requestDto.phone());
        user.setRole(Role.valueOf(requestDto.role()));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
        }

        if (requestDto.companyId() != null) {
            Company company = companyRepository.findByIdAndDeletedAtIsNull(requestDto.companyId())
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + requestDto.companyId()));
            user.setCompany(company);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User deactivateUser(Long id) {

        User user = validateUserExistsAndReturnUser(id);

        user.setIsActive(false);

        return userRepository.save(user);

    }

    @Override
    @Transactional
    public User activateUser(Long id) {

        User user = validateUserExistsAndReturnUser(id);

        user.setIsActive(true);

        return userRepository.save(user);

    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        deleteUser(id, false);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, boolean physicalDelete) {
        User user = findByIdOrThrow(id);

        if (Boolean.TRUE.equals(user.getIsSystemUser())) {
            throw new ForbiddenOperationException("System user cannot be deleted");
        }

        if (physicalDelete) {
            userRepository.delete(user);
        } else {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public Page<User> search(String role, Long companyId, String username, String email, Boolean isActive, Pageable pageable) {
        Specification<User> spec = UserSpecs.notDeleted();
        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecs.byRole(role));
        }
        if (companyId != null) {
            spec = spec.and(UserSpecs.byCompany(companyId));
        }
        if (username != null && !username.isBlank()) {
            spec = spec.and(UserSpecs.usernameContains(username));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecs.emailContains(email));
        }
        if (isActive != null) {
            spec = spec.and(UserSpecs.byIsActive(isActive));
        }
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public List<User> listByCompany(Long companyId) {
        return userRepository.findByCompany_IdAndDeletedAtIsNull(companyId);
    }

    @Override
    public List<AssignableUserDto> listAssignableByCompanyIds(Set<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findByCompany_IdInAndDeletedAtIsNullAndIsActiveTrue(companyIds).stream()
                .map(u -> new AssignableUserDto(u.getId(), u.getUsername()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User updateProfilePicture(Long userId, String profilePictureUrl) {
        User user = findByIdOrThrow(userId);
        user.setProfilePictureUrl(profilePictureUrl);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User resetPassword(Long id, String newPassword) {
        User user = findByIdOrThrow(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
    }

    private void validateUniqueEmailOnCreate(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
    }

    private User findByIdOrThrow(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    private User validateUserExistsAndReturnUser(Long id) {
        return findByIdOrThrow(id);
    }

    private Company resolveCompanyForUser(UserRequestDto requestDto) {
        if (requestDto.companyId() == null) {
            return null;
        }

        return companyRepository.findByIdAndDeletedAtIsNull(requestDto.companyId())
                .orElseThrow(() ->
                        new CompanyNotFoundException("Company not found with id: " + requestDto.companyId())
                );
    }


    private void validateUpdateConstraints(UserRequestDto requestDto, User user) {

        if (!requestDto.username().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsername(requestDto.username())) {
                throw new UserAlreadyExistsException("Username already exists: " + requestDto.username());
            }
        }

        if (!requestDto.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNullAndIdNot(requestDto.email(), user.getId())) {
                throw new EmailAlreadyExistsException("Email already exists: " + requestDto.email());
            }
        }
    }

}
