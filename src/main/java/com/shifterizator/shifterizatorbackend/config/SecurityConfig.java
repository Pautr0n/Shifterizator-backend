package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        auth
                                // Public endpoints
                                .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/health")
                                .permitAll()
                                // Swagger / OpenAPI documentation (no auth required to view docs)
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                // Companies: POST only SUPERADMIN, DELETE only SUPERADMIN, activate/deactivate only SUPERADMIN
                                // GET/PUT for SUPERADMIN+COMPANYADMIN (method-level checks needed for company ownership)
                                // Order matters: specific patterns must come before general ones
                                .requestMatchers(HttpMethod.PATCH, "/api/companies/*/activate", "/api/companies/*/deactivate")
                                .hasRole("SUPERADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/companies").hasRole("SUPERADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasRole("SUPERADMIN")
                                .requestMatchers("/api/companies/**").hasAnyRole("SUPERADMIN", "COMPANYADMIN")

                                // Availability: POST/PUT/DELETE only SUPERADMIN+COMPANYADMIN+SHIFTMANAGER
                                // GET accessible to all authenticated (method-level checks needed for company/location ownership)
                                .requestMatchers(HttpMethod.POST, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/availability/**").authenticated()

                                // Employees: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN+SHIFTMANAGER
                                // GET accessible to all authenticated (method-level checks needed)
                                // PUT /employees/{id}/preferences also allowed for EMPLOYEE (own only - method-level check)
                                .requestMatchers(HttpMethod.POST, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER", "EMPLOYEE")
                                .requestMatchers(HttpMethod.DELETE, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/employees/**").authenticated()

                                // Positions: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/positions/**").authenticated()

                                // Users: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN, GET all authenticated
                                // Method-level checks needed: COMPANYADMIN cannot create COMPANYADMIN users,
                                // cannot delete themselves
                                .requestMatchers(HttpMethod.POST, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/users/**").authenticated()

                                // Languages: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/languages/**").authenticated()

                                // Locations: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/locations/**").authenticated()

                                // Shift templates: POST/DELETE SUPERADMIN+COMPANYADMIN, PUT SUPERADMIN+COMPANYADMIN+SHIFTMANAGER
                                // GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/shift-templates/**").authenticated()

                                // Shift instances: POST (generate-month, schedule-day) SUPERADMIN+COMPANYADMIN+SHIFTMANAGER
                                // POST/PUT/DELETE SUPERADMIN+COMPANYADMIN+SHIFTMANAGER, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/shift-instances/generate-month",
                                        "/api/shift-instances/schedule-day", "/api/shift-instances/schedule-month")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.POST, "/api/shift-instances/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/shift-instances/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/shift-instances/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/shift-instances/**").authenticated()

                                // Shift assignments: POST/DELETE SUPERADMIN+COMPANYADMIN+SHIFTMANAGER, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/shift-assignments/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/shift-assignments/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/shift-assignments/**").authenticated()

                                // Blackout days: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN+SHIFTMANAGER, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/blackout-days/**").authenticated()

                                // Opening hours: POST/PUT/DELETE SUPERADMIN+COMPANYADMIN+SHIFTMANAGER, GET all authenticated
                                .requestMatchers(HttpMethod.POST, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/opening-hours/**").authenticated()

                                // Test endpoints
                                .requestMatchers("/api/test/superadmin").hasRole("SUPERADMIN")
                                .requestMatchers("/api/test/companyadmin").hasRole("COMPANYADMIN")
                                .requestMatchers("/api/test/shiftmanager").hasRole("SHIFTMANAGER")
                                .requestMatchers("/api/test/readonlymanager").hasRole("READONLYMANAGER")
                                .requestMatchers("/api/test/employee").hasRole("EMPLOYEE")

                                // Auth endpoints (me, change-password) - all authenticated
                                .requestMatchers("/api/auth/**").authenticated()

                                // Default: all other requests require authentication
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
