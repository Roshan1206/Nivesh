package com.nivesh.authentication.service;

import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.LoginResponse;
import com.nivesh.authentication.dto.response.RegisterResponse;

/**
 * Interface for handling Authentications.
 *
 * @author Roshan
 */
public interface AuthService {

    /**
     * Register user after validating request body
     *
     * @param request info for creating user
     * @return email and tokens
     */
    RegisterResponse registerUser(RegisterRequest request);


    /**
     * Login user after validating request body
     *
     * @param request user credentials
     * @return tokens
     */
    LoginResponse loginUser(LoginRequest request);


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
