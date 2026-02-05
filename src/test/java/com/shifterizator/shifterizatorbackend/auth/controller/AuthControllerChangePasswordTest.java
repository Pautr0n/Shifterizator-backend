package com.shifterizator.shifterizatorbackend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.user.service.ChangePasswordUserService;
import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.user.exception.InvalidPasswordException;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerChangePasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private ChangePasswordUserService changePasswordUserService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    void changePassword_should_return_200() throws Exception {

        ChangePasswordRequestDto dto = new ChangePasswordRequestDto(
                "OldPass1!",
                "NewPass1!"
        );

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(user);

        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(changePasswordUserService).changeOwnPassword(user, dto);
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

        when(currentUserService.getCurrentUser()).thenReturn(user);

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
