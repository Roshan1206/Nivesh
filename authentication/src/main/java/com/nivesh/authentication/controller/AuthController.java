package com.nivesh.authentication.controller;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.TokenResponse;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.service.AuthService;
import com.nivesh.library.dto.response.OtpResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles endpoints that are excluded from authentication filters.
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
     * Starts registration for a new user and returns the OTP request identifier.
     *
     * @param registerRequest info required for registration
     * @return OTP request identifier used to verify registration
     */
    @PostMapping("/register")
    public ResponseEntity<OtpResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        OtpResponse response = authService.initiateRegistration(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Completes registration after validating the OTP sent during the initial request.
     *
     * @param requestId OTP request identifier returned by the registration initiation endpoint
     * @param otp one-time password submitted as plain text
     * @return registered user email with access and refresh tokens
     */
    @PostMapping(value = "/register/verify/{requestId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<RegisterResponse> registerUser(@PathVariable String requestId, @RequestBody String otp) {
        RegisterResponse response = authService.registerUser(requestId, otp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Authenticates a user and returns issued tokens.
     *
     * @param request body containing email and password
     * @return access and refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.loginUser(request));
    }


    /**
     * Issues a replacement access token from a refresh token.
     *
     * @param request refresh token
     * @return Access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshReqRes> refreshToken(@RequestBody RefreshReqRes request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshAccessToken(request));
    }


    // TODO: Add OTP validation before accepting password reset requests.
    /**
     * Resets a password for an unauthenticated user after receiving valid login details.
     *
     * @param loginRequest email and replacement password
     * @return success message once the password is updated
     */
    @PatchMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody LoginRequest loginRequest) {
        authService.forgotPassword(loginRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
    }


}
