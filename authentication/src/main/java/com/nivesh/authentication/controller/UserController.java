package com.nivesh.authentication.controller;

import com.nivesh.authentication.dto.request.LogoutRequest;
import com.nivesh.authentication.dto.request.ResetPasswordRequest;
import com.nivesh.authentication.service.RefreshTokenService;
import com.nivesh.authentication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }


    /**
     * Logs out the currently authenticated user and returns a no-content response.
     *
     * @param request The LogoutRequest containing the user identifier to log out.
     * @return ResponseEntity<Void> A no-content response indicating successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revokeRefreshToken(request);
        return ResponseEntity.noContent().build();
    }


    /**
     * Logs out the currently authenticated user from all sessions and returns a no-content response.
     *
     * @param request The LogoutRequest containing the user identifier to log out.
     * @return ResponseEntity<Void> A no-content response indicating successful logout.
     */
    @PostMapping("/logout/all")
    public ResponseEntity<Void> logoutUserFromAllSession(@RequestBody LogoutRequest request) {
        String userId = refreshTokenService.revokeUserAllRefreshToken(request);
        userService.incrementTokenVersion(userId);
        return ResponseEntity.noContent().build();
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
