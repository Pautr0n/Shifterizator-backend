package com.shifterizator.shifterizatorbackend.auth.jwt;

import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil(
                "12345678901234567890123456789012",
                10000,
                20000
        );
    }

    @Test
    void generateAccessToken_should_create_valid_token() {
        User user = new User("john", "mail", "pass", Role.EMPLOYEE, null);
        user.setId(1L);

        String token = jwtUtil.generateAccessToken(user);

        assertThat(token).isNotBlank();

        Jws<Claims> parsed = jwtUtil.parseToken(token);
        assertThat(parsed.getBody().getSubject()).isEqualTo("john");
        assertThat(parsed.getBody().get("uid", Integer.class)).isEqualTo(1);
    }

    @Test
    void generateRefreshToken_should_create_valid_refresh_token() {
        User user = new User("john", "mail", "pass", Role.EMPLOYEE, null);
        user.setId(1L);

        String token = jwtUtil.generateRefreshToken(user);

        assertThat(jwtUtil.isRefreshToken(token)).isTrue();
    }

    @Test
    void getUsername_should_return_subject() {
        User user = new User("john", "mail", "pass", Role.EMPLOYEE, null);
        user.setId(1L);

        String token = jwtUtil.generateAccessToken(user);

        assertThat(jwtUtil.getUsername(token)).isEqualTo("john");
    }

    @Test
    void isRefreshToken_should_return_false_for_access_token() {
        User user = new User("john", "mail", "pass", Role.EMPLOYEE, null);
        user.setId(1L);

        String token = jwtUtil.generateAccessToken(user);

        assertThat(jwtUtil.isRefreshToken(token)).isFalse();
    }


}