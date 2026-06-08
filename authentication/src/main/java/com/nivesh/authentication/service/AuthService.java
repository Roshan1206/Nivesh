package com.nivesh.authentication.service;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.request.ResetPasswordRequest;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.dto.response.TokenResponse;
import com.nivesh.library.dto.response.OtpResponse;

import java.util.UUID;

/**
 * Defines the public authentication workflows exposed by the authentication module.
 *
 * @author Roshan
 */
public interface AuthService {

    /**
     * Starts registration by generating an OTP and returning its request identifier.
     */
    OtpResponse initiateRegistration(RegisterRequest request);

    /**
     * Completes registration after validating the pending request and OTP.
     *
     * @param requestId OTP request identifier returned during registration initiation
     * @param otp plain-text OTP submitted by the user
     * @return email and tokens
     */
    RegisterResponse registerUser(String requestId, String otp);

    /**
     * Authenticates user credentials and returns a token pair.
     *
     * @param request user credentials
     * @return tokens
     */
    TokenResponse loginUser(LoginRequest request);

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request refresh token
     * @return access token
     */
    RefreshReqRes refreshAccessToken(RefreshReqRes request);

    /**
     * Resets the password for an unauthenticated user.
     *
     * @param loginRequest user email and replacement password
     */
    String forgotPassword(LoginRequest loginRequest);

    String validateForgotPassword(String requestId, String otp);

    TokenResponse resetPassword(String requestId, ResetPasswordRequest passwordRequest);
}
