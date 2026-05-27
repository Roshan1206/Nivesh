package com.nivesh.authentication.controller;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.LoginResponse;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles endpoints which does not require authentication.
 * Excluded from filter chain
 *
 * @author Roshan
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Manages authentication
     */
    private final AuthService authService;


    /**
     * Injecting required dependency using CI.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * Register new user. Validates all input
     *
     * @param registerRequest info required for registration
     * @return email with access and refresh token
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Used for log in.
     *
     * @param request body containing email and password
     * @return access and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.loginUser(request));
    }


    /**
     * Refresh access token.
     *
     * @param request refresh token
     * @return Access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshReqRes> refreshToken(@RequestBody RefreshReqRes request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshAccessToken(request));
    }


//    TODO: Add otp validation
    @PatchMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody LoginRequest loginRequest) {
        authService.forgotPassword(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
    }


}
