package com.shifterizator.shifterizatorbackend.auth.service;

import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.RefreshTokenRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidCredentialsException;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;


    @Test
    void login_should_return_tokens_when_credentials_valid() {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(1L);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken(user)).thenReturn("access");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refresh");

        LoginRequestDto dto = new LoginRequestDto("john", "Password1!");

        TokenResponseDto response = authService.login(dto);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.username()).isEqualTo("john");
    }

    @Test
    void login_should_throw_when_user_not_found() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        LoginRequestDto dto = new LoginRequestDto("john", "Password1!");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_should_throw_when_password_invalid() {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequestDto dto = new LoginRequestDto("john", "wrong");

        assertThatThrownBy(() -> authService.login(dto))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_should_return_new_access_token() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto("refreshToken");

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(1L);

        when(jwtUtil.isRefreshToken("refreshToken")).thenReturn(true);
        when(jwtUtil.getUsername("refreshToken")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("newAccess");

        TokenResponseDto response = authService.refresh(dto);

        assertThat(response.accessToken()).isEqualTo("newAccess");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
    }

    @Test
    void refresh_should_throw_when_not_refresh_token() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto("invalid");

        when(jwtUtil.isRefreshToken("invalid")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refresh_should_throw_when_user_not_found() {
        RefreshTokenRequestDto dto = new RefreshTokenRequestDto("refreshToken");

        when(jwtUtil.isRefreshToken("refreshToken")).thenReturn(true);
        when(jwtUtil.getUsername("refreshToken")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(dto))
                .isInstanceOf(UserNotFoundException.class);
    }


}