package com.shifterizator.shifterizatorbackend.auth.service;


import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.RefreshTokenRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.auth.exception.InvalidCredentialsException;
import com.shifterizator.shifterizatorbackend.auth.jwt.JwtUtil;
import com.shifterizator.shifterizatorbackend.user.exception.UserNotFoundException;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public TokenResponseDto login(LoginRequestDto dto) {

        log.info("Login attempt for username: {}", dto.username());

        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for username {}", dto.username());
                    return new InvalidCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for username {}", dto.username());
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("Login successful for username: {}", dto.username());

        return new TokenResponseDto(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCompany() != null ? user.getCompany().getId() : null
        );
    }

    public TokenResponseDto refresh(RefreshTokenRequestDto dto) {

        String token = dto.refreshToken();
        log.info("Refresh token attempt");

        if (!jwtUtil.isRefreshToken(token)) {
            log.warn("Invalid refresh token type");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtUtil.getUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found for refresh token"));

        String newAccessToken = jwtUtil.generateAccessToken(user);

        log.info("Refresh token successful for username: {}", username);

        return new TokenResponseDto(
                newAccessToken,
                token,
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getCompany() != null ? user.getCompany().getId() : null
        );
    }

}
