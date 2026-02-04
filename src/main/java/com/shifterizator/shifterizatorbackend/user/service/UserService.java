package com.shifterizator.shifterizatorbackend.user.service;

import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.exception.UserAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        validateUniqueEmail(requestDto.email());

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
        user.setRole(Role.valueOf(requestDto.role()));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(requestDto.password()));
        }

        // Opcional: permitir cambiar company solo para SUPERADMIN (lo haremos cuando tengamos seguridad)
        if (requestDto.companyId() != null) {
            Company company = companyRepository.findById(requestDto.companyId())
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


    @Transactional
    public void deleteUser(Long id) {
        User user = validateUserExistsAndReturnUser(id);
        userRepository.delete(user);
    }

    public User getUser(Long id) {
        return validateUserExistsAndReturnUser(id);
    }

    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public List<User> listActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    public List<User> listInactiveUsers() {
        return userRepository.findByIsActive(false);
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

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
    }

    private User validateUserExistsAndReturnUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id: " + id)
        );
    }

    private Company resolveCompanyForUser(UserRequestDto requestDto) {
        if (requestDto.companyId() == null) {
            return null; // SUPERADMIN or system-level user
        }

        return companyRepository.findById(requestDto.companyId())
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
            if (userRepository.existsByEmail(requestDto.email())) {
                throw new UserAlreadyExistsException("Email already exists: " + requestDto.email());
            }
        }
    }

}
