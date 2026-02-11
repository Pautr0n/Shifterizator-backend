package com.shifterizator.shifterizatorbackend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import com.shifterizator.shifterizatorbackend.user.exception.UserAlreadyExistsException;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;


@WithMockUser
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class JacksonConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void createUser_should_return_201_and_userResponse() throws Exception {

        UserRequestDto requestDto = new UserRequestDto(
                "john123",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        User user = new User("john123", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        UserResponseDto responseDto = new UserResponseDto(
                10L, "john123", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );

        when(userService.createUser(any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("john123"))
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    @WithMockUser
    void createUser_should_return_409_when_username_exists() throws Exception {

        UserRequestDto requestDto = new UserRequestDto(
                "john",
                "john@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        when(userService.createUser(any()))
                .thenThrow(new UserAlreadyExistsException("Username already exists: john"));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Username already exists: john"));
    }


    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void updateUser_should_return_200_and_updated_user() throws Exception {

        UserRequestDto requestDto = new UserRequestDto(
                "johnUpdated",
                "johnUpdated@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        User user = new User("johnUpdated", "johnUpdated@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        UserResponseDto responseDto = new UserResponseDto(
                10L, "johnUpdated", "johnUpdated@mail.com", null, "EMPLOYEE", null, true, null, null
        );

        when(userService.updateUser(eq(10L), any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johnUpdated"))
                .andExpect(jsonPath("$.email").value("johnUpdated@mail.com"));
    }

    @Test
    @WithMockUser
    void updateUser_should_return_409_when_email_exists() throws Exception {

        UserRequestDto requestDto = new UserRequestDto(
                "john",
                "duplicate@mail.com",
                "Password1!",
                "EMPLOYEE",
                1L,
                null
        );

        Mockito.when(userService.updateUser(eq(10L), any()))
                .thenThrow(new UserAlreadyExistsException("Email already exists: duplicate@mail.com"));

        mockMvc.perform(put("/api/users/10")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Email already exists: duplicate@mail.com"));
    }

    // ---------------------------------------------------------
    // ACTIVATE-DEACTIVATE
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void activateUser_should_return_200_and_userResponse() throws Exception {

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);
        user.setIsActive(true);

        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );

        when(userService.activateUser(10L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(patch("/api/users/10/activate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @WithMockUser
    void deactivateUser_should_return_200_and_userResponse() throws Exception {

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);
        user.setIsActive(false);

        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, false, null, null
        );

        when(userService.deactivateUser(10L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(patch("/api/users/10/deactivate").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void deleteUser_should_return_204() throws Exception {

        mockMvc.perform(delete("/api/users/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(10L, false);
    }

    @Test
    @WithMockUser
    void deleteUser_should_return_404_when_not_found() throws Exception {

        Mockito.doThrow(new UserNotFoundException("User not found with id: 10"))
                .when(userService).deleteUser(10L, false);

        mockMvc.perform(delete("/api/users/10").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: 10"));
    }


    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void getUser_should_return_200_and_user() throws Exception {

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        UserResponseDto responseDto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );

        when(userService.getUser(10L)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        mockMvc.perform(get("/api/users/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    @WithMockUser
    void getUser_should_return_404_when_not_found() throws Exception {

        when(userService.getUser(99L))
                .thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found with id: 99"));
    }

    // ---------------------------------------------------------
    // LIST ALL
    // ---------------------------------------------------------
    @Test
    @WithMockUser
    void listUsers_should_return_200_and_paginated_content() throws Exception {

        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);

        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );

        Pageable pageable = PageRequest.of(0, 10);
        when(userService.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void listUsers_with_isActive_true_returns_active_users() throws Exception {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);
        user.setIsActive(true);
        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.search(any(), any(), any(), any(), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users").param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].isActive").value(true));
    }

    @Test
    @WithMockUser
    void listUsers_with_username_filter_returns_matching_users() throws Exception {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);
        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.search(any(), any(), eq("john"), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users").param("username", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john"));
    }

    @Test
    @WithMockUser
    void listUsers_with_username_and_isActive_returns_filtered_page() throws Exception {
        User user = new User("john", "john@mail.com", "hashed", Role.EMPLOYEE, null);
        user.setId(10L);
        user.setIsActive(true);
        UserResponseDto dto = new UserResponseDto(
                10L, "john", "john@mail.com", null, "EMPLOYEE", null, true, null, null
        );
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.search(any(), any(), eq("john"), any(), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/users")
                        .param("username", "john")
                        .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("john"))
                .andExpect(jsonPath("$.content[0].isActive").value(true));
    }

}