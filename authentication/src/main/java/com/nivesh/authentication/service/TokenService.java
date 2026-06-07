package com.nivesh.authentication.service;

import com.nivesh.authentication.entity.RefreshToken;
import com.nivesh.authentication.entity.User;

/**
 * Interface for managing auth tokens.
 *
 * @author Roshan
 */
public interface TokenService {

    /**
     * Generate access token with different claims based on user status.
     *
     * @param user user entity
     * @param tokenType token to be generated
     * @return Access token
     */
    String generateAccessToken(User user, String tokenType);

    String generateRefreshToken(RefreshToken refreshToken);

    String getUserEmail(String token);
}
