package com.shifterizator.shifterizatorbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shifterizatorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shifterizator API")
                        .description("""
                                RESTful API for Shifterizator - Automatic Multi-Business Schedule Generator
                                
                                This API provides comprehensive workforce management capabilities including:
                                - Multi-tenant company management
                                - Employee and location management
                                - Shift template creation and automatic shift generation
                                - Employee availability and preference management
                                - Shift assignments and scheduling
                                - Blackout days and special opening hours configuration
                                
                                ## Authentication
                                All protected endpoints require JWT Bearer token authentication. 
                                Use the `/api/auth/login` endpoint to obtain an access token.
                                
                                ## Role-Based Access Control
                                The API implements role-based access control with the following hierarchy:
                                - **SUPERADMIN**: Full system access across all companies
                                - **COMPANYADMIN**: Full access within their company
                                - **SHIFTMANAGER**: Management access for assigned locations
                                - **READONLYMANAGER**: Read-only access for assigned locations
                                - **EMPLOYEE**: Self-service access to own data
                                
                                ## Rate Limiting
                                Please be mindful of API usage. Excessive requests may be rate-limited.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Shifterizator Support")
                                .email("paugaston@hotmail.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://github.com/Pautr0n/")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.shifterizator.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT Bearer Token Authentication
                                        
                                        Include the access token in the Authorization header:
                                        ```
                                        Authorization: Bearer <your-access-token>
                                        ```
                                        
                                        Access tokens expire after 15 minutes. Use the refresh token endpoint
                                        to obtain a new access token without re-authenticating.
                                        """)));
    }
}
