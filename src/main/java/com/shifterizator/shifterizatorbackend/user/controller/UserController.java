package com.shifterizator.shifterizatorbackend.user.controller;

import com.shifterizator.shifterizatorbackend.user.dto.ResetPasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "Endpoints for managing users (create, update, activate/deactivate, reset password). " +
                "User creation and company-scoped operations restricted to SUPERADMIN/COMPANYADMIN; reset password typically SUPERADMIN."
)
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Create user", description = "Creates a new user. Restricted to SUPERADMIN or COMPANYADMIN (company-scoped).", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        User created = userService.createUser(requestDto);
        UserResponseDto response = userMapper.toDto(created);

        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(response);
    }


    @Operation(summary = "Update user", description = "Updates an existing user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        User updated = userService.updateUser(id, requestDto);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @Operation(summary = "Activate user", description = "Activates a deactivated user.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto> activateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {

        User user = userService.activateUser(id);

        return ResponseEntity.ok(userMapper.toDto(user));

    }

    @Operation(summary = "Deactivate user", description = "Deactivates a user (soft disable).", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto> deactivateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {

        User user = userService.deactivateUser(id);

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @Operation(summary = "Delete user", description = "Deletes a user. Use physical=true for hard delete.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Parameter(description = "If true, physically delete from DB") @RequestParam(defaultValue = "false") boolean physical
    ) {
        userService.deleteUser(id, physical);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset password (admin)", description = "Resets a user's password. Typically restricted to SUPERADMIN.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequestDto dto
    ) {
        userService.resetPassword(id, dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by ID.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @Operation(
            summary = "List users (paginated)",
            description = "Paginated list with optional filters: role, companyId, username (partial match), email (partial match), isActive. Use e.g. ?isActive=true for active only, ?username=john for search by username.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Page of users") })
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> listUsers(
            @Parameter(description = "Filter by role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by company ID") @RequestParam(required = false) Long companyId,
            @Parameter(description = "Filter by username (partial match)") @RequestParam(required = false) String username,
            @Parameter(description = "Filter by email (partial match)") @RequestParam(required = false) String email,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        Page<User> page = userService.search(role, companyId, username, email, isActive, pageable);
        return ResponseEntity.ok(page.map(userMapper::toDto));
    }

    @Operation(summary = "List users by company", description = "Returns all users belonging to a company.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "List of users") })
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserResponseDto>> listUsersByCompany(
            @Parameter(description = "Company ID", required = true) @PathVariable Long companyId) {
        List<User> users = userService.listByCompany(companyId);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

}
