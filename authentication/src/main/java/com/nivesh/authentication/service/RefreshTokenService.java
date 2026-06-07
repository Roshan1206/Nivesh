package com.nivesh.authentication.service;

import com.nivesh.authentication.dto.request.LogoutRequest;
import com.nivesh.authentication.entity.User;

/**
 * Contracts for handling refresh token.
 */
public interface RefreshTokenService {

    /**
     * Issue new refresh token for user.
     *
     * @param user user for which the token issued.
     */
    String issueRefreshToken(User user);


    /**
     * Revoke current authenticated user
     *
     * @param request Reason and refresh token
     */
    void revokeRefreshToken(LogoutRequest request);


    /**
     * Revoke all refresh token for a user
     */
    void revokeUserAllRefreshToken(LogoutRequest request);
}
