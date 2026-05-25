package com.nivesh.library.service;

import java.util.Map;

/**
 * Interface for handling and validating jwt token.
 *
 * @author Roshan
 */
public interface JwtTokenService {

    /**
     * Validates refresh token including signature, expiration and type
     *
     * @param refreshToken Refresh token
     */
    void validateRefreshToken(String refreshToken);

    /**
     * Extract the user details from jwt token which are required to create customer account.
     *
     * @return User details
     */
    Map<String, String> getUserInfo();

    /**
     * Extracts the email of current authenticated user
     *
     * @param token authenticated token
     * @return User email
     */
    String extractEmail(String token);

    /**
     * Extract token type from token
     *
     * @return Token type
     */
    String getTokenType();

    /**
     * Validates if the token is expired or not.
     *
     * @param token jwt token
     * @return true - if token is valid
     */
    boolean isTokenExpired(String token);

    /**
     * Extract current authenticated user id.
     *
     * @return User ID
     */
    String getUserId();
}
