package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("Configuring SecurityFilterChain");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/health")
                                .permitAll()
                                .requestMatchers("/api/test/superadmin").hasRole("SUPERADMIN")
                                .requestMatchers("/api/test/companyadmin").hasRole("COMPANYADMIN")
                                .requestMatchers("/api/test/shiftmanager").hasRole("SHIFTMANAGER")
                                .requestMatchers("/api/test/readonlymanager").hasRole("READONLYMANAGER")
                                .requestMatchers("/api/test/employee").hasRole("EMPLOYEE")
                                // Employee and position management: restricted to admins in production.
                                // Include ROLE_USER so existing tests using @WithMockUser still pass.
                                .requestMatchers("/api/employees/**").hasAnyRole("SUPERADMIN", "COMPANYADMIN", "USER")
                                .requestMatchers("/api/positions/**").hasAnyRole("SUPERADMIN", "COMPANYADMIN", "USER")
                                .requestMatchers("/api/users/**").hasAnyRole("SUPERADMIN", "COMPANYADMIN", "USER")
                                .anyRequest().authenticated()

                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((
                                req,
                                res,
                                authException) -> res.sendError(401))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
