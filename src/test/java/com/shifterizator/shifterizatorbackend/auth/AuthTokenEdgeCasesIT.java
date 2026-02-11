package com.shifterizator.shifterizatorbackend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.shifterizator.shifterizatorbackend.BaseIntegrationTest;
import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.RefreshTokenRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for authentication and token edge cases:
 * - missing / invalid / expired JWT in Authorization header
 * - refresh token flow and invalid refresh token handling.
 */
class AuthTokenEdgeCasesIT extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String JWT_SECRET =
            "63yYuYleXPvNFkojq6peEcbwFS8Kbb5NerVz5Sapqk+Q/rMdWm3PUDNb8siO/yh1UkQibSOhrPUDdIt1dCs5fQ==";

    @Test
    @DisplayName("Accessing protected endpoint without token returns 401")
    void accessingProtectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Accessing protected endpoint with invalid token returns 401 and INVALID_TOKEN error")
    void accessingProtectedEndpointWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("Accessing protected endpoint with expired token returns 401 and TOKEN_EXPIRED error")
    void accessingProtectedEndpointWithExpiredTokenReturns401() throws Exception {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        long now = System.currentTimeMillis();

        String expiredToken = Jwts.builder()
                .setSubject("superadmin")
                .setIssuedAt(new Date(now - 2000))
                .setExpiration(new Date(now - 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"));
    }

    @Test
    @DisplayName("Refresh token flow returns new access token that can access protected endpoint")
    void refreshTokenFlowReturnsNewAccessToken() throws Exception {
        // Login and obtain tokens
        LoginRequestDto loginRequest = new LoginRequestDto("superadmin", "SuperAdmin1!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        TokenResponseDto initialTokens = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );

        // Use refresh token to obtain a new access token
        RefreshTokenRequestDto refreshRequest =
                new RefreshTokenRequestDto(initialTokens.refreshToken());

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        TokenResponseDto refreshedTokens = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );

        assertThat(refreshedTokens.accessToken()).isNotBlank();
        assertThat(refreshedTokens.refreshToken()).isEqualTo(initialTokens.refreshToken());

        // New access token should allow access to protected endpoint
        String bearerToken = "Bearer " + refreshedTokens.accessToken();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("superadmin"));
    }

    @Test
    @DisplayName("Using access token as refresh token returns 401 and INVALID_REFRESH_TOKEN error")
    void usingAccessTokenAsRefreshTokenReturns401() throws Exception {
        // Login and obtain tokens
        LoginRequestDto loginRequest = new LoginRequestDto("superadmin", "SuperAdmin1!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        TokenResponseDto initialTokens = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponseDto.class
        );

        // Try to use the access token as if it were a refresh token
        RefreshTokenRequestDto badRefreshRequest =
                new RefreshTokenRequestDto(initialTokens.accessToken());

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRefreshRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("INVALID_REFRESH_TOKEN");
    }
}

