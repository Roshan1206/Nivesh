package com.nivesh.authentication.dto.response;

/**
 * Access and refresh token pair returned after successful authentication.
 */
public record TokenResponse(String accessToken, String refreshToken) {
}
