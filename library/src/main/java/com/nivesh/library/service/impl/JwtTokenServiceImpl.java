package com.nivesh.library.service.impl;

import com.nivesh.library.constant.Constants;
import com.nivesh.library.service.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of JWT token.
 *
 * @author Roshan
 */
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    /**
     * Servlet request for extracting headers.
     */
    private final HttpServletRequest servletRequest;

    /**
     * Jwt decoder for validation
     */
    private final JwtDecoder jwtDecoder;


    /**
     * Injecting required dependency via Constructor Injection
     */
    public JwtTokenServiceImpl(HttpServletRequest servletRequest, JwtDecoder jwtDecoder) {
        this.servletRequest = servletRequest;
        this.jwtDecoder = jwtDecoder;
    }


    /**
     * Extract the user details from jwt token which are required to create customer account.
     *
     * @return User details
     */
    @Override
    public Map<String, String> getUserInfo() {
        Map<String, String> info = new HashMap<>();
        String token = getToken();
        Map<String, Object> claims = jwtDecoder.decode(token).getClaims();
        info.put(Constants.USER_ID, getUserId());
        info.put(Constants.EMAIL, (String) claims.get(Constants.EMAIL));
        info.put(Constants.MOBILE, (String) claims.get(Constants.MOBILE));
        return info;
    }


    /**
     * Extracts the email of current authenticated user after token validation.
     *
     * @param token authenticated token
     * @return User email
     */
    @Override
    public String extractEmail(String token) {
        return jwtDecoder.decode(token).getClaimAsString(Constants.EMAIL);
    }

    /**
     * Extracts the email of current authenticated user
     *
     * @return User email
     */
    @Override
    public String extractEmail() {
        return extractEmail(getToken());
    }

    /**
     * Extract token type from token
     *
     * @return Token type
     */
    @Override
    public String getTokenType() {
        return jwtDecoder.decode(getToken()).getClaimAsString(Constants.TOKEN_TYPE);
    }

    @Override
    public boolean isTokenExpired(String refreshToken) {
        Instant expiry = jwtDecoder.decode(refreshToken).getExpiresAt();
        return expiry != null && expiry.isBefore(Instant.now());
    }


    /**
     * Extract current authenticated user id.
     *
     * @return User ID
     */
    @Override
    public String getUserId() {
        return jwtDecoder.decode(getToken()).getSubject();
    }

    /**
     * Extract token from request header.
     *
     * @return JWT token
     */
    private String getToken() {
        String token = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            throw new InvalidBearerTokenException("Invalid token. Login again");
        }
        return token.substring(7);
    }

}
