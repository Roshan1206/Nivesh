package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.config.properties.AuthCacheProperties;
import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.request.ResetPasswordRequest;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.dto.response.TokenResponse;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.exception.UserAlreadyExistsException;
import com.nivesh.authentication.exception.InvalidUserStatusException;
import com.nivesh.authentication.service.RefreshTokenService;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.entity.enums.CustomerStatus;
import com.nivesh.authentication.service.AuthService;
import com.nivesh.authentication.service.UserService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.exception.CacheNotFoundException;
import com.nivesh.library.exception.OtpErrorCode;
import com.nivesh.library.exception.OtpException;
import com.nivesh.library.exception.SessionExpiredException;
import com.nivesh.library.service.OtpCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Coordinates registration, login, refresh-token, and password-reset workflows.
 *
 * @author Roshan
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final String LOGIN_CACHE_NAME = "login";

    private static final String REGISTER_CACHE_NAME = "register";

    /**
     * Verifies submitted credentials against Spring Security's user details pipeline.
     */
    private final AuthenticationManager authenticationManager;

    /** Cache settings used by authentication flows. */
    private final AuthCacheProperties cacheProperties;

    /** Cache used to store login OTP state. */
    private final CacheManager cacheManager;

    /** Service used to create and validate OTP cache entries. */
    private final OtpCacheService otpCacheService;

    private final RefreshTokenService refreshTokenService;

    /**
     * Issues and reads JWTs used by the authentication API.
     */
    private final TokenService tokenService;

    /**
     * Manages user lookup, creation, status changes, and password updates.
     */
    private final UserService userService;

    /**
     * Injects authentication dependencies required for login, registration, and token refresh flows.
     */
    public AuthServiceImpl(AuthenticationManager authenticationManager, AuthCacheProperties cacheProperties,
                           CacheManager cacheManager, OtpCacheService otpCacheService,
                           TokenService tokenService, RefreshTokenService refreshTokenService,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.cacheProperties = cacheProperties;
        this.cacheManager = cacheManager;
        this.otpCacheService = otpCacheService;
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    /**
     * Starts registration by generating an OTP and caching the request until verification.
     *
     * @param request user information captured before OTP verification
     */
    @Override
    public OtpResponse initiateRegistration(RegisterRequest request) {
        if (userService.isUserExistByEmail(request.getEmail())){
            log.error("Email already exists. Please Register it  with different email");
            throw new UserAlreadyExistsException("Email already exists. Please Register it  with different email");
        }
        if (userService.isUserExistByMobile(request.getMobileNumber())){
            log.error("Mobile number already exists. Please Register it  with different mobile number");
            throw new UserAlreadyExistsException("Mobile number already exists. Please Register it  with different mobile number");
        }
        String requestId = UUID.randomUUID().toString();
        otpCacheService.generateAndSendOtp(requestId, request.getEmail());
        getCache(REGISTER_CACHE_NAME).put(requestId, request);
        log.debug("Registration initiation completed for requestId: {}", requestId);
        return new OtpResponse(requestId);
    }

    /**
     * Creates a user after the cached registration request and submitted OTP are validated.
     *
     * @param requestId OTP request identifier returned during registration initiation
     * @param otp plain-text OTP provided by the user
     * @return email and tokens
     */
    @Transactional
    @Override
    public RegisterResponse registerUser(String requestId, String otp) {
        log.debug("Initiating auth registration for requestId: {}", requestId);
        RegisterRequest registerRequest = getCache(REGISTER_CACHE_NAME).get(requestId, RegisterRequest.class);
        if (registerRequest == null) {
            log.error("Register cache not found for requestId: {}", requestId);
            throw new OtpException("Cache expired", OtpErrorCode.EXPIRED);
        }
        otpCacheService.validateOtp(requestId, otp);
        User savedUser = userService.createNewUser(registerRequest);
        String accessToken = tokenService.generateAccessToken(savedUser, Constants.ONBOARDED_TOKEN);
        String refreshToken = refreshTokenService.issueRefreshToken(savedUser);
        log.debug("Auth registration completed successfully.");
        return new RegisterResponse(registerRequest.getEmail(), new TokenResponse(accessToken, refreshToken));
    }

    /**
     * Authenticates a user, applies status-specific token rules, and tracks failed attempts.
     *
     * @param request user credentials
     * @return tokens
     */
    @Override
    public TokenResponse loginUser(LoginRequest request) {
        String tokenType;
        String email = request.getEmail();
        User user = userService.getUserByEmail(email);
        UUID key = user.getId();
        Integer loginAttempts = getCache(LOGIN_CACHE_NAME).get(user.getId(), Integer.class);

        tokenType = getTokenType(user);

        int maxAttempts = cacheProperties.getMaxAttempts();
        if (loginAttempts != null && loginAttempts >= maxAttempts) {
            lockUser(user);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException e) {
            Integer failedAttempt = getCache(LOGIN_CACHE_NAME).get(key, Integer.class);
            failedAttempt = failedAttempt == null ? 1 : failedAttempt + 1;
            getCache(LOGIN_CACHE_NAME).put(key, failedAttempt);
            if (failedAttempt >= maxAttempts) {
                lockUser(user);
            }
            if (e instanceof BadCredentialsException){
                int remainingAttempts = maxAttempts - failedAttempt;
                String msg = "Invalid username or password. " + remainingAttempts + " attempts remaining";
                throw new BadCredentialsException(msg);
            }
            throw e;
        }
        getCache(LOGIN_CACHE_NAME).evict(key);

        String accessToken = tokenService.generateAccessToken(user, tokenType);
        String refreshToken = refreshTokenService.issueRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken);
    }


    /**
     * Locks a user account by setting the 'locked' flag to true.
     *
     * @param user The User object to lock.
     */
    private void lockUser(User user) {
        user.setCustomerStatus(CustomerStatus.LOCKED);
        user.setLockedUntil(Instant.now().plus(cacheProperties.getLockDurationInHour(), ChronoUnit.HOURS));
        userService.save(user);
        getCache("user").put(user.getId(), user);
        throw new LockedException("Account locked due to too many failed attempts");
    }

    /**
     * Reactivates eligible locked or deactivated users, otherwise blocks login.
     */
    private String validateCustomerStatus(User user, CustomerStatus customerStatus) {
        if (customerStatus.isEqual(CustomerStatus.LOCKED)) {
            if (user.getLockedUntil().isBefore(Instant.now())) {
                userService.updateStatus(user, CustomerStatus.ACTIVE);
            } else {
                throw new LockedException("User is currently locked. Please try after " +
                        user.getLockedUntil().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        }
        if (customerStatus.isEqual(CustomerStatus.DEACTIVATED)) {
            if (user.getUpdatedAt().isAfter(Instant.now().minus(30, ChronoUnit.DAYS))) {
                userService.updateStatus(user, CustomerStatus.ACTIVE);
            } else {
                throw new InvalidUserStatusException("User is currently deactivates, Please visit branch to get account active again");
            }
        }
        return Constants.ACCESS_TOKEN;
    }

    /**
     * Issues a new access token after validating the supplied refresh token.
     *
     * @param request refresh token
     * @return access token
     */
    @Transactional
    @Override
    public RefreshReqRes refreshAccessToken(RefreshReqRes request) {
        String userId = refreshTokenService.validateRefreshToken(request.getToken());
        User user = userService.getUser(userId);
        String token = tokenService.generateAccessToken(user, getTokenType(user));
        return new RefreshReqRes(token);
    }

    /**
     * Resets the password for an unauthenticated user.
     *
     * @param loginRequest user email and replacement password
     */
    @Override
    public String forgotPassword(LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());
        UUID requestId = UUID.randomUUID();
        otpCacheService.generateAndSendOtp(requestId.toString(), user.getEmail());
        return requestId.toString();
    }


    @Override
    public String validateForgotPassword(String requestId, String otp) {
        otpCacheService.validateOtp(requestId, otp);
        String key = UUID.randomUUID().toString();
        getCache("reset-password").put(key, key);
        return key;
    }

    @Override
    public TokenResponse resetPassword(String requestId, ResetPasswordRequest passwordRequest) {
        String userRequestId = getCache("reset-password").get(requestId, String.class);
        if (!requestId.equals(userRequestId)) {
            throw new SessionExpiredException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        userService.resetPassword(passwordRequest);
        User user = userService.getUserByEmail(passwordRequest.getEmail());
        String tokenType = getTokenType(user);
        String accessToken = tokenService.generateAccessToken(user, tokenType);
        String refreshToken = refreshTokenService.issueRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken);
    }


    private String getTokenType(User user) {
        String tokenType;
        tokenType = switch (user.getCustomerStatus()) {
            case ONBOARDED -> Constants.ONBOARDED_TOKEN;
            case ACTIVE -> Constants.ACCESS_TOKEN;
            case LOCKED, DEACTIVATED -> validateCustomerStatus(user, user.getCustomerStatus());
            case REGISTERED -> Constants.REGISTERED_TOKEN;
        };
        return tokenType;
    }


    /**
     * Retrieves a cache instance based on the provided cache name.
     *
     * @param cacheName The name of the cache to retrieve.
     * @return A Cache object representing the retrieved cache.
     */
    private Cache getCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException(cacheName);
        }
        return cache;
    }

}
