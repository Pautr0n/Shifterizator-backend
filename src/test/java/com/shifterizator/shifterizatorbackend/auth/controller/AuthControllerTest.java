package com.shifterizator.shifterizatorbackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.exception.InvalidPasswordException;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.service.ChangePasswordUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ChangePasswordUserService changePasswordUserService;

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
    @WithMockUser
    void changePassword_should_return_200() throws Exception {

        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "NewPass1!"
        );

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        when(authService.getAuthenticatedUser()).thenReturn(user);

        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(changePasswordUserService).changeOwnPassword(user, dto);
    }

    @Test
    @WithMockUser
    void changePassword_should_return_400_when_current_password_invalid() throws Exception {

        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "WrongPass1!",
                "NewPass1!"
        );

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        when(authService.getAuthenticatedUser()).thenReturn(user);

        doThrow(new InvalidPasswordException("Current password is incorrect"))
                .when(changePasswordUserService).changeOwnPassword(user, dto);

        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

}