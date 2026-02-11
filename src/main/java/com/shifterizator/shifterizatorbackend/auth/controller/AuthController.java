package com.shifterizator.shifterizatorbackend.auth.controller;

import com.shifterizator.shifterizatorbackend.auth.dto.LoginRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.RefreshTokenRequestDto;
import com.shifterizator.shifterizatorbackend.auth.dto.TokenResponseDto;
import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.auth.service.CurrentUserService;
import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.service.ChangePasswordUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Endpoints for user authentication, token management, and password changes"
)
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final ChangePasswordUserService changePasswordUserService;

    @Operation(
            summary = "User login",
            description = """
                    Authenticates a user with username and password, returning JWT access and refresh tokens.
                    
                    **Use Case:** First step in the authentication flow. Use the returned access token
                    in the Authorization header for all subsequent API requests.
                    
                    **Token Expiration:**
                    - Access token: 15 minutes
                    - Refresh token: 7 days
                    
                    **Example Flow:**
                    1. Call this endpoint with user credentials
                    2. Receive access and refresh tokens
                    3. Include access token in Authorization header: `Bearer <accessToken>`
                    4. When access token expires, use refresh token endpoint to get a new one
                    """,
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                      "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
                                      "userId": 2,
                                      "username": "admin",
                                      "role": "COMPANYADMIN",
                                      "companyId": 1
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials or validation error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "INVALID_CREDENTIALS",
                                      "message": "Invalid credentials"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        log.info("POST /api/auth/login");
        TokenResponseDto response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh access token",
            description = """
                    Obtains a new access token using a valid refresh token. This allows users to continue
                    accessing the API without re-authenticating when their access token expires.
                    
                    **Use Case:** Called automatically by client applications when the access token expires
                    (typically after 15 minutes). The refresh token remains valid for 7 days.
                    
                    **Best Practice:** Implement automatic token refresh in your client application to
                    provide a seamless user experience.
                    """,
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "INVALID_REFRESH_TOKEN",
                                      "message": "Invalid refresh token"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody RefreshTokenRequestDto dto) {
        log.info("POST /api/auth/refresh");
        TokenResponseDto response = authService.refresh(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get current user information",
            description = """
                    Returns basic information about the currently authenticated user based on the JWT token
                    in the Authorization header.
                    
                    **Use Case:** Verify authentication status and get current user context.
                    """,
            tags = {"Authentication"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user information retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "username": "admin"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token"
            )
    })
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(Map.of("username", user.getUsername()));
    }

    @Operation(
            summary = "Change current user's password",
            description = """
                    Allows the authenticated user to change their own password. Requires providing
                    the current password for security verification.
                    
                    **Password Requirements:**
                    - Length: 8-20 characters
                    - Must contain: lowercase letter, uppercase letter, number, and special character
                    
                    **Use Case:** User-initiated password change for security purposes.
                    """,
            tags = {"Authentication"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid current password or new password doesn't meet requirements",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Password must contain lowercase, uppercase, number and special character"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token"
            )
    })
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto dto) {

        User user = currentUserService.getCurrentUser();

        changePasswordUserService.changeOwnPassword(user, dto);

        return ResponseEntity.ok().build();
    }

}
