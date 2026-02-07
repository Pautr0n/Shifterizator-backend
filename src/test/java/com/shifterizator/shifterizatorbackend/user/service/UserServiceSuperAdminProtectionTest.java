package com.shifterizator.shifterizatorbackend.user.service;

import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.user.exception.ForbiddenOperationException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceSuperAdminProtectionTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private UserService service;

    @Test
    void deleteUser_should_throw_when_user_is_system_user() {

        User founder = new User("superadmin", "mail", "hash", Role.SUPERADMIN, null);
        founder.setId(1L);
        founder.setIsSystemUser(true);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(founder));

        assertThatThrownBy(() -> service.deleteUser(1L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("System user cannot be deleted");

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_should_allow_deleting_non_system_superadmin() {

        User otherSuperAdmin = new User("super2", "mail", "hash", Role.SUPERADMIN, null);
        otherSuperAdmin.setId(2L);
        otherSuperAdmin.setIsSystemUser(false);

        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(otherSuperAdmin));
        lenient().when(userRepository.save(any(User.class))).thenReturn(otherSuperAdmin);

        service.deleteUser(2L, true);

        verify(userRepository).delete(otherSuperAdmin);
    }

}
