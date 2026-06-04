package com.nivesh.library.constant;

public class CacheConstants {

    /** Name of the OTP cache shared by the cache manager and OTP service. */
    public static final String OTP_CACHE_NAME = "otp";

    public static final String TRANSACTION_CACHE_NAME = "transaction";

    /** Cache that keeps pending registration requests until OTP verification completes. */
    public static final String REGISTER_CACHE_NAME = "register";

    /** Cache that tracks failed login attempts while an account can be locked. */
    public static final String LOGIN_CACHE_NAME = "login";
}
