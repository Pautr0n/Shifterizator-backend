package com.shifterizator.shifterizatorbackend.auth.controller;

import com.shifterizator.shifterizatorbackend.user.dto.ChangePasswordRequestDto;
import com.shifterizator.shifterizatorbackend.user.service.ChangePasswordUserService;
import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.user.model.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final ChangePasswordUserService changePasswordUserService;

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        User authenticatedUser = authService.getAuthenticatedUser();

        changePasswordUserService.changeOwnPassword(authenticatedUser, dto);

        return ResponseEntity.ok().build();
    }
}
