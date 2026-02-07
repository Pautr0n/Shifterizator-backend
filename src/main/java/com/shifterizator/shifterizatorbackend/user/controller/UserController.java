package com.shifterizator.shifterizatorbackend.user.controller;

import com.shifterizator.shifterizatorbackend.user.dto.ResetPasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserRequestDto;
import com.shifterizator.shifterizatorbackend.user.dto.UserResponseDto;
import com.shifterizator.shifterizatorbackend.user.mapper.UserMapper;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.service.UserService;
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
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
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


    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto requestDto
    ) {
        User updated = userService.updateUser(id, requestDto);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    // ---------------------------------------------------------
    // ACTIVATE-DEACTIVATE
    // ---------------------------------------------------------
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto> activateUser(@PathVariable Long id) {

        User user = userService.activateUser(id);

        return ResponseEntity.ok(userMapper.toDto(user));

    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long id) {

        User user = userService.deactivateUser(id);

        return ResponseEntity.ok(userMapper.toDto(user));
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean physical
    ) {
        userService.deleteUser(id, physical);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // RESET PASSWORD (admin)
    // ---------------------------------------------------------
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequestDto dto
    ) {
        userService.resetPassword(id, dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    // ---------------------------------------------------------
    // LIST (paginated with optional filters)
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        Page<User> page = userService.search(role, companyId, email, isActive, pageable);
        return ResponseEntity.ok(page.map(userMapper::toDto));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserResponseDto>> listUsersByCompany(@PathVariable Long companyId) {
        List<User> users = userService.listByCompany(companyId);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponseDto>> listActiveUsers() {
        List<User> users = userService.listActiveUsers();
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<UserResponseDto>> listInactiveUsers() {
        List<User> users = userService.listInactiveUsers();
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsersByUsername(
            @RequestParam String username
    ) {
        List<User> users = userService.searchUsersByUsername(username);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/search/email")
    public ResponseEntity<List<UserResponseDto>> searchUsersByEmail(@RequestParam String email) {
        List<User> users = userService.searchUsersByEmail(email);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/search/active")
    public ResponseEntity<List<UserResponseDto>> searchActiveUsersByUsername(
            @RequestParam String username
    ) {
        List<User> users = userService.searchActiveUsersByUsername(username);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

    @GetMapping("/search/inactive")
    public ResponseEntity<List<UserResponseDto>> searchInactiveUsersByUsername(
            @RequestParam String username
    ) {
        List<User> users = userService.searchInactiveUsersByUsername(username);
        return ResponseEntity.ok(users.stream().map(userMapper::toDto).toList());
    }

}
