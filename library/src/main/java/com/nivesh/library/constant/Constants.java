package com.nivesh.library.constant;

/**
 * Centralized constants used across the Nivesh library.
 * All reusable values should be defined here instead of hardcoding.
 *
 * @author Roshan
 */
public final class Constants {

    /** JWT claim defining the token type */
    public static final String TOKEN_TYPE = "token_type";

    /** Token type for access tokens (short-lived) */
    public static final String ACCESS_TOKEN = "ACCESS";

    /** Token type for refresh tokens (long-lived) */
    public static final String REFRESH_TOKEN = "REFRESH";

    /** Token type for onboarded users */
    public static final String ONBOARDED_TOKEN = "ONBOARDED";

    /** Token type for registered but not yet onboarded users */
    public static final String REGISTERED_TOKEN = "REGISTERED";
    
    // ==================== JWT Claims - Permissions ====================
    /** JWT claim containing user roles */
    public static final String ROLES = "roles";

    /** JWT claim containing user permissions */
    public static final String PERMISSIONS = "permissions";

    /** Super admin role with full permissions */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    
    /** JWT claim for user status */
    public static final String STATUS = "status";
    /** JWT claim for user email */
    public static final String EMAIL = "email";
    /** JWT claim for user mobile number */
    public static final String MOBILE = "mobileNumber";
    /** JWT claim for user ID */
    public static final String USER_ID = "userId";
    
    /** Header name for identifying internal service requests */
    public static final String INTERNAL_ROLE_HEADER_NAME = "X-Internal-Role";

    /** Header value required for internal service authentication */
    public static final String INTERNAL_ROLE_HEADER_VALUE = "INTERNAL_SERVICE";

    /** Header name to identify source microservice */
    public static final String SOURCE_SERVICE_HEADER_NAME = "X-Source-Service";

    public static final String IDEMPOTENCY_KEY = "idempotencyKey";
}
