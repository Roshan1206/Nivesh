package com.nivesh.authentication.controller;

import com.nivesh.authentication.dto.request.LogoutRequest;
import com.nivesh.authentication.service.RefreshTokenService;
import com.nivesh.authentication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Rest controller for user.
 * Responsible for changes in user entity.
 *
 * @author Roshan
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final RefreshTokenService refreshTokenService;

    /**
     * Responsible for managing user
     */
    private final UserService userService;

    /**
     * Injecting required dependency via CI
     */
    public UserController(RefreshTokenService refreshTokenService, UserService userService) {
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request);
        return ResponseEntity.ok().build();
    }

    /**
     * internal endpoint for changing user status.
     */
    @PostMapping("/internal/{userId}/{status}")
    public ResponseEntity<String> updateUserStatus(@PathVariable String userId, @PathVariable String status) {
        String token = userService.updateStatus(userId, status);
        return ResponseEntity.ok(token);
    }
}
