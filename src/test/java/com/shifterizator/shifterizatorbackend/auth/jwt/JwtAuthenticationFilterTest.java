package com.shifterizator.shifterizatorbackend.auth.jwt;


import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void should_authenticate_when_token_valid() throws Exception {
        User user = new User("john", "mail", "pass", Role.EMPLOYEE, null);
        user.setId(1L);

        when(jwtUtil.getUsername("validToken")).thenReturn("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_401_when_token_invalid() throws Exception {
        when(jwtUtil.getUsername("invalidToken"))
                .thenThrow(new io.jsonwebtoken.JwtException("Invalid"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));
    }

    @Test
    void should_return_401_when_token_expired() throws Exception {
        when(jwtUtil.getUsername("expiredToken"))
                .thenThrow(new ExpiredJwtException(null, null, "Expired"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer expiredToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"));
    }

    @Test
    void should_return_401_when_user_not_found() throws Exception {
        when(jwtUtil.getUsername("validToken")).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer validToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTH_ERROR"));
    }

    @Test
    void should_pass_through_when_no_token_on_public_endpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_401_when_no_token_on_protected_endpoint() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }


}