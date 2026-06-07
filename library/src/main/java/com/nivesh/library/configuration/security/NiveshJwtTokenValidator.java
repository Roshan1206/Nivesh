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

        Integer tokenVersion = token.getClaim("tok_ver");
        String userId = token.getSubject();
        if (tokenVersion != null && userId != null) {
            Integer currentVersion = getTokenCurrentVersion(userId);
            if (currentVersion != null && tokenVersion < currentVersion) {
                return OAuth2TokenValidatorResult.failure(TOKEN_VERSION_STALE);
            }
        }
        return OAuth2TokenValidatorResult.success();
    }

    private Integer getTokenCurrentVersion(String userId) {
        String val = redisTemplate.opsForValue().get("tok_ver:" + userId);
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isBlackListed(String jti) {
        return redisTemplate.hasKey("blacklist:jti:" + jti);
    }
}
