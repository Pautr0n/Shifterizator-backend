package com.shifterizator.shifterizatorbackend.user.service;


import com.shifterizator.shifterizatorbackend.company.exception.CompanyNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.exception.EmailAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper ;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private  CompanyRepository companyRepository;
    @InjectMocks
    private UserService service;

    // ---------------------------------------------------------
    // CREATE USER
    // ---------------------------------------------------------
    @Test
    void createUser_should_create_user_when_valid() {

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

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, company);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("john@mail.com")).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(company));
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
        when(userMapper.toEntity(dto, "hashed", company)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User result = service.createUser(dto);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    void createUser_should_create_user_when_valid_without_companyId() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                null,
                null
        );


        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("john@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("hashed");
        when(userMapper.toEntity(dto, "hashed", null)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        User result = service.createUser(dto);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }


    @Test
    void createUser_should_throw_when_username_exists() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                5L,
                null
        );

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists: john");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_should_throw_when_email_exists() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                5L,
                null
        );

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("john@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: john@mail.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_should_throw_when_company_not_found() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                99L,
                null
        );

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNull("john@mail.com")).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found with id: 99");

        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------
    // UPDATE USER
    // ---------------------------------------------------------
    @Test
    void updateUser_should_update_when_valid() {

        UserRequestDto dto = new UserRequestDto(
                "johnUpdated",
                "updated@mail.com",
                "Password1!",
                "EMPLOYEE",
                5L,
                null
        );

        Company company = new Company();
        company.setId(5L);

        User existing = new User("john", "john@mail.com", "oldHash", Role.EMPLOYEE, null);
        existing.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("johnUpdated")).thenReturn(false);
        when(userRepository.existsByEmailAndDeletedAtIsNullAndIdNot("updated@mail.com", 10L)).thenReturn(false);
        when(companyRepository.findByIdAndDeletedAtIsNull(5L)).thenReturn(Optional.of(company));
        when(passwordEncoder.matches("Password1!", "oldHash")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("newHash");
        when(userRepository.save(existing)).thenReturn(existing);

        User result = service.updateUser(10L, dto);

        assertThat(result.getUsername()).isEqualTo("johnUpdated");
        assertThat(result.getEmail()).isEqualTo("updated@mail.com");
        assertThat(result.getPassword()).isEqualTo("newHash");
        assertThat(result.getCompany()).isEqualTo(company);
    }

    @Test
    void updateUser_should_throw_when_user_not_found() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                null,
                null
        );

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(10L, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 10");
    }

    @Test
    void updateUser_should_throw_when_username_exists() {

        UserRequestDto dto = new UserRequestDto(
                "newName",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                null,
                null
        );

        User existing = new User("oldName", "john@mail.com", "hash", Role.EMPLOYEE, null);
        existing.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername("newName")).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(10L, dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists: newName");
    }

    @Test
    void updateUser_should_throw_when_email_exists() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "new@mail.com",
                "Password1!",
                "EMPLOYEE",
                null,
                null
        );

        User existing = new User("john", "old@mail.com", "hash", Role.EMPLOYEE, null);
        existing.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailAndDeletedAtIsNullAndIdNot("new@mail.com", 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(10L, dto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: new@mail.com");
    }

    @Test
    void updateUser_should_throw_when_company_not_found() {

        UserRequestDto dto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                99L,
                null
        );

        User existing = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        existing.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(existing));
        when(companyRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser(10L, dto))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Company not found with id: 99");
    }

    // ---------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // ---------------------------------------------------------
    @Test
    void deactivateUser_should_set_isActive_false() {

        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        user.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = service.deactivateUser(10L);

        assertThat(result.getIsActive()).isFalse();
    }

    @Test
    void activateUser_should_set_isActive_true() {

        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        user.setId(10L);
        user.setIsActive(false);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = service.activateUser(10L);

        assertThat(result.getIsActive()).isTrue();
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void deleteUser_should_logical_delete_when_exists() {

        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        user.setId(10L);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.deleteUser(10L);

        verify(userRepository).save(user);
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteUser_should_throw_when_not_found() {

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUser(10L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 10");
    }

    // ---------------------------------------------------------
    // GET USER
    // ---------------------------------------------------------
    @Test
    void getUser_should_return_user_when_exists() {

        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(user));

        User result = service.getUser(10L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUser_should_throw_when_not_found() {

        when(userRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(10L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 10");
    }

    // ---------------------------------------------------------
    // SEARCH (paginated with filters: role, companyId, username, email, isActive)
    // ---------------------------------------------------------
    @Test
    void search_should_return_page_with_no_filters() {
        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<User> result = service.search(null, null, null, null, null, pageable);

        assertThat(result.getContent()).containsExactly(user);
        assertThat(result.getTotalElements()).isOne();
    }

    @Test
    void search_should_filter_by_username() {
        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<User> result = service.search(null, null, "john", null, null, pageable);

        assertThat(result.getContent()).containsExactly(user);
    }

    @Test
    void search_should_filter_by_isActive_true() {
        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        user.setIsActive(true);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<User> result = service.search(null, null, null, null, true, pageable);

        assertThat(result.getContent()).containsExactly(user);
    }

    @Test
    void search_should_filter_by_username_and_isActive() {
        User user = new User("john", "john@mail.com", "hash", Role.EMPLOYEE, null);
        user.setIsActive(true);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<User> result = service.search(null, null, "john", null, true, pageable);

        assertThat(result.getContent()).containsExactly(user);
    }

}