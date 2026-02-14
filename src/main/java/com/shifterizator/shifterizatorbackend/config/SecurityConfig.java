package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.auth.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, 
                         Environment environment,
                         CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.environment = environment;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("Configuring SecurityFilterChain");

        boolean isProd = environment.matchesProfiles("prod");
        log.info("Active profile: {}", isProd ? "prod" : "dev/test");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/health")
                            .permitAll(                    );

                    if (!isProd) {
                        auth.requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll();
                        log.info("Swagger UI enabled (non-prod environment)");
                    } else {
                        log.info("Swagger UI disabled (production environment)");
                    }
                    
                    auth
                                .requestMatchers(HttpMethod.PATCH, "/api/companies/*/activate", "/api/companies/*/deactivate")
                                .hasRole("SUPERADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/companies").hasRole("SUPERADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/companies/**").hasRole("SUPERADMIN")
                                .requestMatchers("/api/companies/**").hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/availability/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/availability/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER", "EMPLOYEE")
                                .requestMatchers(HttpMethod.DELETE, "/api/employees/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/employees/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/positions/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/positions/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/api/users/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/users/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/languages/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/languages/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/locations/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers("/api/locations/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/shift-templates/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/shift-templates/**").authenticated()
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
                                .requestMatchers(HttpMethod.POST, "/api/shift-assignments/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/shift-assignments/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/shift-assignments/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/blackout-days/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/blackout-days/**").authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.PUT, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers(HttpMethod.DELETE, "/api/opening-hours/**")
                                .hasAnyRole("SUPERADMIN", "COMPANYADMIN", "SHIFTMANAGER")
                                .requestMatchers("/api/opening-hours/**").authenticated()
                                .requestMatchers("/api/test/superadmin").hasRole("SUPERADMIN")
                                .requestMatchers("/api/test/companyadmin").hasRole("COMPANYADMIN")
                                .requestMatchers("/api/test/shiftmanager").hasRole("SHIFTMANAGER")
                                .requestMatchers("/api/test/readonlymanager").hasRole("READONLYMANAGER")
                                .requestMatchers("/api/test/employee").hasRole("EMPLOYEE")
                                .requestMatchers("/api/auth/**").authenticated()
                                .anyRequest().authenticated();
                })
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
