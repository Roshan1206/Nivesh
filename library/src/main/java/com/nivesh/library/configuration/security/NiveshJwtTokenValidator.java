package com.nivesh.library.configuration.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class NiveshJwtTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error JTI_BLACKLISTED =
            new OAuth2Error("token_revoked", "Access token has been revoked", null);

    private static final OAuth2Error TOKEN_VERSION_STALE =
            new OAuth2Error("token_version_invalid", "Session Invalidated. Please login again", null);

    private final RedisTemplate<String, String> redisTemplate;

    public NiveshJwtTokenValidator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verify the validity and/or constraints of the provided OAuth 2.0 Token.
     *
     * @param token an OAuth 2.0 token
     * @return OAuth2TokenValidationResult the success or failure detail of the validation
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti != null && isBlackListed(jti)) {
            return OAuth2TokenValidatorResult.failure(JTI_BLACKLISTED);
        }

        Long tokenVersion = token.getClaim("tok_ver");
        String userId = token.getSubject();
        if (tokenVersion != null && userId != null) {
            Long currentVersion = getTokenCurrentVersion(userId);
            if (currentVersion != null && tokenVersion < currentVersion) {
                return OAuth2TokenValidatorResult.failure(TOKEN_VERSION_STALE);
            }
        }
        return OAuth2TokenValidatorResult.success();
    }


    /**
     * Retrieves the current version associated with a given user ID from the token data.
     *
     * @param userId The user ID to retrieve the version for.
     * @return The current version integer, or null if not found.
     */
    private Long getTokenCurrentVersion(String userId) {
        String val = redisTemplate.opsForValue().get("tok_ver:" + userId);
        if (val == null) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException exception) {
            return null;
        }
    }


    /**
     * Checks if a JWT token ID is present in the blacklisted set of tokens.
     *
     * @param jti The JWT token ID to check.
     * @return True if the token ID is found in the blacklist, false otherwise.
     */
    private boolean isBlackListed(String jti) {
        return redisTemplate.hasKey("blacklist:jti:" + jti);
    }
}
