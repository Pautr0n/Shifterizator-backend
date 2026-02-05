package com.shifterizator.shifterizatorbackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.RefreshTokenRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.auth.exception.AuthException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidCredentialsException;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidRefreshTokenException;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import com.shifterizator.shifterizatorbackend.user.service.ChangePasswordUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private ChangePasswordUserService changePasswordUserService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class JacksonConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void login_should_return_tokens_when_credentials_valid() throws Exception {

        TokenResponseDto response = new TokenResponseDto(
                "access",
                "refresh",
                1L,
                "john",
                "EMPLOYEE",
                null
        );

        when(authService.login(any())).thenReturn(response);

        LoginRequestDto request = new LoginRequestDto("john", "Password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void login_should_return_4xx_when_invalid_credentials() throws Exception {

        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        LoginRequestDto request = new LoginRequestDto("john", "wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void refresh_should_return_200_and_new_access_token() throws Exception {

        TokenResponseDto response = new TokenResponseDto(
                "newAccess",
                "refreshToken",
                1L,
                "john",
                "EMPLOYEE",
                null
        );

        when(authService.refresh(any())).thenReturn(response);

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"));
    }

    @Test
    void refresh_should_return_401_when_refresh_token_invalid() throws Exception {

        when(authService.refresh(any()))
                .thenThrow(new InvalidRefreshTokenException("Invalid refresh token"));

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("badToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_should_return_401_when_user_not_found() throws Exception {

        when(authService.refresh(any()))
                .thenThrow(new AuthException("User not found"));

        RefreshTokenRequestDto request = new RefreshTokenRequestDto("refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}