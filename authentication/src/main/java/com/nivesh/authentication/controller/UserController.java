package com.nivesh.authentication.controller;

import com.nivesh.authentication.service.UserService;
import org.springframework.http.HttpStatus;
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

    /**
     * Responsible for managing user
     */
    private final UserService userService;

    /**
     * Injecting required dependency via CI
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * internal endpoint for changing user status.
     */
    @PostMapping("/internal/{userId}/{status}")
    public ResponseEntity<String> updateUserStatus(@PathVariable String userId, @PathVariable String status) {
        String token = userService.updateStatus(userId, status);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}
