package com.shifterizator.shifterizatorbackend.user.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private CompanyRepository companyRepository;


    public User createUser(UserRequestDto requestDto) {
        validateUniqueUsername(requestDto.username());
        validateUniqueEmailOnCreate(requestDto.email());

        Company company = resolveCompanyForUser(requestDto);

        String hashedPassword = passwordEncoder.encode(requestDto.password());

        User newUser = userMapper.toEntity(requestDto, hashedPassword, company);

        return userRepository.save(newUser);
    }


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

        // Opcional: permitir cambiar company solo para SUPERADMIN (lo haremos cuando tengamos seguridad)
        if (requestDto.companyId() != null) {
            Company company = companyRepository.findByIdAndDeletedAtIsNull(requestDto.companyId())
                    .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + requestDto.companyId()));
            user.setCompany(company);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(Long id) {

        User user = validateUserExistsAndReturnUser(id);

        user.setIsActive(false);

        return userRepository.save(user);

    }

    @Transactional
    public User activateUser(Long id) {

        User user = validateUserExistsAndReturnUser(id);

        user.setIsActive(true);

        return userRepository.save(user);

    }


    /**
     * Delete user (logical delete by default). Use {@link #deleteUser(Long, boolean)} for physical delete.
     */
    @Transactional
    public void deleteUser(Long id) {
        deleteUser(id, false);
    }

    /**
     * Delete user: logical (set deletedAt) when physicalDelete is false, physical when true.
     * Only SUPERADMIN should call with physicalDelete=true; COMPANYADMIN uses logical delete.
     */
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

    public User getUser(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    /**
     * Paginated search with optional filters: role, companyId, email, isActive. Excludes logically deleted users.
     */
    public Page<User> search(String role, Long companyId, String email, Boolean isActive, Pageable pageable) {
        Specification<User> spec = UserSpecs.notDeleted();
        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecs.byRole(role));
        }
        if (companyId != null) {
            spec = spec.and(UserSpecs.byCompany(companyId));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecs.emailContains(email));
        }
        if (isActive != null) {
            spec = spec.and(UserSpecs.byIsActive(isActive));
        }
        return userRepository.findAll(spec, pageable);
    }

    public List<User> listByCompany(Long companyId) {
        return userRepository.findByCompany_IdAndDeletedAtIsNull(companyId);
    }

    public List<User> searchUsersByEmail(String email) {
        return userRepository.findByEmailContainingIgnoreCaseAndDeletedAtIsNull(email);
    }

    @Transactional
    public User resetPassword(Long id, String newPassword) {
        User user = findByIdOrThrow(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public List<User> listAllUsers() {
        return userRepository.findAll(UserSpecs.notDeleted());
    }

    public List<User> listActiveUsers() {
        Specification<User> spec = UserSpecs.notDeleted().and(UserSpecs.byIsActive(true));
        return userRepository.findAll(spec);
    }

    public List<User> listInactiveUsers() {
        Specification<User> spec = UserSpecs.notDeleted().and(UserSpecs.byIsActive(false));
        return userRepository.findAll(spec);
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public List<User> searchActiveUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCaseAndIsActive(username, true);
    }

    public List<User> searchInactiveUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCaseAndIsActive(username, false);
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
            return null; // SUPERADMIN or system-level user
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
