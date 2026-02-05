package com.shifterizator.shifterizatorbackend.auth.service;

import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_should_return_user_when_authenticated() {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                null,
                List.of(() -> "EMPLOYEE")
        );


        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        User result = currentUserService.getCurrentUser();

        assertThat(result.getUsername()).isEqualTo("john");
    }

    @Test
    void getCurrentUser_should_throw_when_not_authenticated() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCurrentUser_should_throw_when_user_not_found() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john",
                null,
                List.of(() -> "ROLE_EMPLOYEE")
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class);
    }

}