package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.config.properties.TokenProperties;
import com.nivesh.authentication.entity.RefreshToken;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service class for generating token
 *
 * @author Roshan
 */
@Service
@EnableConfigurationProperties(TokenProperties.class)
public class TokenServiceImpl implements TokenService {

    /** Configured issuer URL used in generated tokens. */
    @Value("${nivesh.auth.url}")
    private String issuerUrl;

    /** Encoder used to sign JWT values. */
    private final JwtEncoder jwtEncoder;

    /** Service used to create or inspect JWT values. */
    private final JwtTokenService jwtTokenService;

    /** Token configuration values. */
    private final TokenProperties tokenProperties;

    /**
     * Injects token services and properties required to issue JWTs.
     */
    public TokenServiceImpl(JwtEncoder jwtEncoder, JwtTokenService jwtTokenService, TokenProperties tokenProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtTokenService = jwtTokenService;
        this.tokenProperties = tokenProperties;
    }

    /**
     * Generate access token with different claims based on user status.
     *
     * @param user user entity
     * @param tokenType token to be generated
     * @return Access token
     */
    @Override
    public String generateAccessToken(User user, String tokenType) {
        String accessToken = switch (tokenType) {
            case Constants.ONBOARDED_TOKEN -> issueOnboardedToken(user);
            case Constants.ACCESS_TOKEN, Constants.REGISTERED_TOKEN -> issueAccessToken(user);
            default -> "";
        };
        return accessToken;
    }

    @Override
    public String generateRefreshToken(RefreshToken refreshToken) {
        int expiry = Integer.parseInt(tokenProperties.refreshExpiry());

        Map<String, Object> claims = new HashMap<>();
        claims.put("token_id", refreshToken.getTokenId());
        claims.put(Constants.TOKEN_TYPE, Constants.REFRESH_TOKEN);

        return generateToken(claims, expiry, ChronoUnit.DAYS, refreshToken.getUser().getId().toString());
    }

    @Override
    public String getUserEmail(String refreshToken) {
        return jwtTokenService.extractEmail(refreshToken);
    }

    /**
     * Issues onboarded token. User is only allowed to perform customer registration
     */
    private String issueOnboardedToken(User user) {
        int expiry = Integer.parseInt(tokenProperties.onboardedExpiry());
        Map<String, Object> claims = addClaims(user);
        claims.put(Constants.TOKEN_TYPE, Constants.ONBOARDED_TOKEN);
        claims.put(Constants.MOBILE, user.getMobileNumber());
        return generateToken(claims, expiry, ChronoUnit.MINUTES, String.valueOf(user.getId()));
    }

    /**
     * Issues Registered token. User is only allowed to start the kyc process.
     */
    private String issueRegisteredToken(User user) {
        int expiry = Integer.parseInt(tokenProperties.accessExpiry());
        Map<String, Object> claims = addClaims(user);
        claims.put(Constants.TOKEN_TYPE, Constants.ACCESS_TOKEN);
        return generateToken(claims, expiry, ChronoUnit.MINUTES, String.valueOf(user.getId()));
    }

    /**
     * Issues access token. User is allowed to perform all action in his account.
     */
    private String issueAccessToken(User user) {
        int expiry = Integer.parseInt(tokenProperties.accessExpiry());
        Map<String, Object> claims = addClaims(user);
        claims.put(Constants.TOKEN_TYPE, Constants.ACCESS_TOKEN);
        return generateToken(claims, expiry, ChronoUnit.MINUTES, String.valueOf(user.getId()));
    }

    /**
     * Generates different kind of jwt token.
     * {@code generateToken(claims, 10, ChronoUnit.MINUTES, userId)} will generate token with expiration of 10 min.
     *
     * @param claims claims to be added in token.
     * @param expiryTime time duration
     * @param expiryUnit unit of time for expiration
     * @param userId uuid of user
     * @return jwt token
     */
    private String generateToken(Map<String, Object> claims, int expiryTime, ChronoUnit expiryUnit, String userId) {
        JwtClaimsSet.Builder tokenBuilder = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .issuer(issuerUrl)
                .subject(userId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(expiryTime, expiryUnit));

        claims.forEach(tokenBuilder::claim);
        JwtClaimsSet token = tokenBuilder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(token)).getTokenValue();
    }

    /**
     * Add common claims in the token. Token related claims should be added separately.
     *
     * @param user extracts info and add in claims
     * @return Map containing claims
     */
    private Map<String, Object> addClaims(User user) {
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        user.getAuthorities().forEach(auth -> {
            String val = auth.getAuthority();
            if (val.contains(":")) {
                permissions.add(val);
            } else {
                roles.add(val);
            }
        });

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.ROLES, roles);
        claims.put(Constants.PERMISSIONS, permissions);
        claims.put(Constants.EMAIL, user.getEmail());
        claims.put("jti", UUID.randomUUID());
        return claims;
    }
}
