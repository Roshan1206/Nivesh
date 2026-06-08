package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.config.properties.TokenProperties;
import com.nivesh.authentication.dto.request.LogoutRequest;
import com.nivesh.authentication.entity.RefreshToken;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.repository.RefreshTokenRepository;
import com.nivesh.authentication.service.RefreshTokenService;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.authentication.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages refresh token and revoke users
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    /** Decodes jwt token */
    private final JwtDecoder jwtDecoder;

    /** Cache used for revocation */
    private final RedisTemplate<String, String> redisTemplate;

    /** DAO layer for refresh tokens */
    private final RefreshTokenRepository repository;

    /** Token properties */
    private final TokenProperties tokenProperties;

    /** Issues tokens */
    private final TokenService tokenService;

    /** Manages user*/
    private final UserService userService;


    /**
     * Injecting required dependency
     */
    public RefreshTokenServiceImpl(RefreshTokenRepository repository, JwtDecoder jwtDecoder,
                                   TokenProperties tokenProperties, UserService userService,
                                   TokenService tokenService, RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.tokenProperties = tokenProperties;
        this.tokenService = tokenService;
        this.userService = userService;
        this.jwtDecoder = jwtDecoder;
        this.redisTemplate = redisTemplate;
    }


    /**
     * Issues a new refresh token for the given user.
     *
     * @param user The user for whom to issue a refresh token.
     * @return The newly issued refresh token.
     */
    @Override
    public String issueRefreshToken(User user) {
        UUID tokenId = UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .user(user)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(Long.parseLong(tokenProperties.refreshExpiry()), ChronoUnit.DAYS))
                .build();
        RefreshToken saved = repository.save(refreshToken);
        return tokenService.generateRefreshToken(saved);
    }


    /**
     * Revokes a refresh token associated with the provided logout request.
     *
     * @param request The LogoutRequest containing the refresh token to revoke.
     */
    @Transactional
    @Override
    public void revokeRefreshToken(LogoutRequest request) {
        String userId = getUserId();
        String tokenId = jwtDecoder.decode(request.getRefreshToken()).getClaimAsString("token_id");
        repository.revokeUser(UUID.fromString(userId), "Logout", UUID.fromString(tokenId));
        blacklistAccessToken(getToken(), userId);
    }


    /**
     * Revokes all refresh tokens associated with a given user ID.
     *
     * @param request LogoutRequest object containing the user ID to revoke tokens for.
     */
    @Transactional
    @Override
    public void revokeUserAllRefreshToken(LogoutRequest request) {
        String userId = getUserId();
        repository.revokeAllByUser(UUID.fromString(userId), "Logout");
        userService.incrementTokenVersion(userId);
    }


    /** Get user id of current authenticated user */
    private String getUserId() {
        return jwtDecoder.decode(getToken()).getSubject();
    }


    /** Get token for current authenticated user */
    private static String getToken() {
        String token = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            token = jwtAuthenticationToken.getToken().getTokenValue();
        }
        return token;
    }


    /** Blacklist current authenticated user in cache */
    private void blacklistAccessToken(String token, String userId) {
        String jti = jwtDecoder.decode(token).getClaimAsString("jti");
        long timeout = Long.parseLong(tokenProperties.accessExpiry());
        redisTemplate.opsForValue().set("blacklist:jti:" + jti, userId, timeout, TimeUnit.MINUTES);
    }
}
