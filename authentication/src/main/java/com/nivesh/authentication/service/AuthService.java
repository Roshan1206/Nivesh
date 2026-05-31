package com.nivesh.authentication.service;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.LoginResponse;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.dto.response.TokenResponse;
import com.nivesh.library.dto.response.OtpResponse;

/**
 * Interface for handling Authentications.
 *
 * @author Roshan
 */
public interface AuthService {

    /**
     * Initiate the user registration with otp.
     */
    OtpResponse initiateRegistration(RegisterRequest request);

    /**
     * Register user after validating request body
     *
     * @param request info for creating user
     * @return email and tokens
     */
    RegisterResponse registerUser(String requestId, String otp);


    /**
     * Login user after validating request body
     *
     * @param request user credentials
     * @return tokens
     */
    TokenResponse loginUser(LoginRequest request);


    /**
     * Refresh access token
     *
     * @param request refresh token
     * @return access token
     */
    RefreshReqRes refreshAccessToken(RefreshReqRes request);

    /**
     * Reset the password for unauthenticated user
     *
     * @param loginRequest user login info
     */
    void forgotPassword(LoginRequest loginRequest);
}
