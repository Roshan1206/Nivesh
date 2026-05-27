package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.LoginResponse;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.exception.InvalidUserStatusException;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.library.entity.enums.CustomerStatus;
import com.nivesh.authentication.service.AuthService;
import com.nivesh.authentication.service.UserService;
import com.nivesh.library.constant.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service class for managing user authentications.
 *
 * @author Roshan
 */
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * Used for authentication
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Responsible for tokens operations.
     */
    private final TokenService tokenService;

    /**
     * Responsible for managing users
     */
    private final UserService userService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, TokenService tokenService,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
    }


    /**
     * Register user after validating request body
     *
     * @param request info for creating user
     * @return email and tokens
     */
    @Transactional
    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        User savedUser = userService.createNewUser(request);
        String accessToken = tokenService.generateAccessToken(savedUser, Constants.ONBOARDED_TOKEN);
        String refreshToken = tokenService.generateRefreshToken(request.getEmail(), String.valueOf(savedUser.getId()));
        return new RegisterResponse(request.getEmail(), accessToken, refreshToken);
    }


    /**
     * Login user after validating request body
     *
     * @param request user credentials
     * @return tokens
     */
    @Transactional
    @Override
    public LoginResponse loginUser(LoginRequest request) {
        String tokenType;
        String email = request.getEmail();
        User user = userService.getUserByEmail(email);
        CustomerStatus customerStatus = user.getCustomerStatus();

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        tokenType = switch (customerStatus) {
            case ONBOARDED -> Constants.ONBOARDED_TOKEN;
            case ACTIVE -> Constants.ACCESS_TOKEN;
            case LOCKED, DEACTIVATED -> validateCustomerStatus(user, customerStatus);
            case REGISTERED -> Constants.REGISTERED_TOKEN;
        };
        String accessToken = tokenService.generateAccessToken(user, tokenType);
        String refreshToken = tokenService.generateRefreshToken(email, String.valueOf(user.getId()));
        return new LoginResponse(accessToken, refreshToken);
    }

    private String validateCustomerStatus(User user, CustomerStatus customerStatus) {
        if (customerStatus.isEqual(CustomerStatus.LOCKED)) {
            if (user.getLockedUntil().isBefore(LocalDateTime.now())) {
                userService.updateStatus(user, CustomerStatus.ACTIVE);
            } else {
                throw new InvalidUserStatusException("User is currently locked. Please try after " + user.getLockedUntil());
            }
        }
        if (customerStatus.isEqual(CustomerStatus.DEACTIVATED)) {
            if (user.getUpdatedAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                userService.updateStatus(user, CustomerStatus.ACTIVE);
            } else {
                throw new InvalidUserStatusException("User is currently deactivates, Please visit branch to get account active again");
            }
        }
        return Constants.ACCESS_TOKEN;
    }


    /**
     * Get new access token after validating refresh token.
     *
     * @param request refresh token
     * @return access token
     */
    @Transactional
    @Override
    public RefreshReqRes refreshAccessToken(RefreshReqRes request) {
        String email = tokenService.getUserEmail(request.getToken());
        User user = userService.getUserByEmail(email);
        String token = tokenService.generateAccessToken(user, Constants.ACCESS_TOKEN);
        return new RefreshReqRes(token);
    }


    /**
     * Reset the password for unauthenticated user
     *
     * @param loginRequest user login info
     */
    @Override
    public void forgotPassword(LoginRequest loginRequest) {
        userService.forgotPassword(loginRequest);
    }
}
