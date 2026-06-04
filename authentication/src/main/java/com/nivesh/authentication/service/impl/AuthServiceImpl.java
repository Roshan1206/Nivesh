package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.config.properties.AuthCacheProperties;
import com.nivesh.authentication.config.resource.AuthCacheConfiguration;
import com.nivesh.authentication.dto.RefreshReqRes;
import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.response.RegisterResponse;
import com.nivesh.authentication.dto.response.TokenResponse;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.exception.InvalidUserStatusException;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.entity.enums.CustomerStatus;
import com.nivesh.authentication.service.AuthService;
import com.nivesh.authentication.service.UserService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.entity.enums.OtpPurpose;
import com.nivesh.library.exception.OtpErrorCode;
import com.nivesh.library.exception.OtpException;
import com.nivesh.library.cache.OtpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

    /**
     * Verifies submitted credentials against Spring Security's user details pipeline.
     */
    private final AuthenticationManager authenticationManager;

    private final AuthCacheProperties cacheProperties;

    private final Cache loginCache;

    private final Cache registerCache;

    private final OtpCacheService otpCacheService;

    private final OtpSender sender;

    /**
     * Issues and reads JWTs used by the authentication API.
     */
    private final TokenService tokenService;

    /**
     * Manages user lookup, creation, status changes, and password updates.
     */
    private final UserService userService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, AuthCacheProperties cacheProperties,
                           @Qualifier("authCacheManager") CacheManager cacheManager, OtpCacheService otpCacheService,
                           @Qualifier("emailOtpSender") OtpSender sender, TokenService tokenService,
                           UserService userService) {
        this.authenticationManager = authenticationManager;
        this.cacheProperties = cacheProperties;
        this.loginCache = cacheManager.getCache(AuthCacheConfiguration.LOGIN_CACHE_NAME);
        this.registerCache = cacheManager.getCache(AuthCacheConfiguration.REGISTER_CACHE_NAME);
        this.otpCacheService = otpCacheService;
        this.sender = sender;
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
        String requestId = UUID.randomUUID().toString();
        otpCacheService.generateOtp(requestId, OtpPurpose.USER_REGISTRATION, request.getEmail());
        registerCache.put(OtpCacheService.buildKey(requestId, OtpPurpose.USER_REGISTRATION), request);
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
        String key = OtpCacheService.buildKey(requestId, OtpPurpose.USER_REGISTRATION);
        RegisterRequest registerRequest = registerCache.get(key, RegisterRequest.class);
        if (registerRequest == null) {
            throw new OtpException("Otp expired", OtpErrorCode.EXPIRED);
        }
        otpCacheService.validateOtp(requestId, OtpPurpose.USER_REGISTRATION, otp);
        User savedUser = userService.createNewUser(registerRequest);
        String accessToken = tokenService.generateAccessToken(savedUser, Constants.ONBOARDED_TOKEN);
        String refreshToken = tokenService.generateRefreshToken(registerRequest.getEmail(), String.valueOf(savedUser.getId()));
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
        CustomerStatus customerStatus = user.getCustomerStatus();
        String key = buildKey(user.getId().toString(), "LOGIN");
        Integer loginAttempts = loginCache.get(key, Integer.class);

        tokenType = switch (customerStatus) {
            case ONBOARDED -> Constants.ONBOARDED_TOKEN;
            case ACTIVE -> Constants.ACCESS_TOKEN;
            case LOCKED, DEACTIVATED -> validateCustomerStatus(user, customerStatus);
            case REGISTERED -> Constants.REGISTERED_TOKEN;
        };

        int maxAttempts = cacheProperties.getMaxAttempts();
        if (loginAttempts != null && loginAttempts >= maxAttempts) {
            lockUser(user);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (AuthenticationException e) {
            Integer failedAttempt = loginCache.get(key, Integer.class);
            failedAttempt = failedAttempt == null ? 1 : failedAttempt + 1;
            loginCache.put(key, failedAttempt);
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
        loginCache.evict(key);

        String accessToken = tokenService.generateAccessToken(user, tokenType);
        String refreshToken = tokenService.generateRefreshToken(email, String.valueOf(user.getId()));
        return new TokenResponse(accessToken, refreshToken);
    }

    private void lockUser(User user) {
        user.setCustomerStatus(CustomerStatus.LOCKED);
        user.setLockedUntil(Instant.now().plus(cacheProperties.getLockDurationMin(), ChronoUnit.MINUTES));
        userService.save(user);
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
        String email = tokenService.getUserEmail(request.getToken());
        User user = userService.getUserByEmail(email);
        String token = tokenService.generateAccessToken(user, Constants.ACCESS_TOKEN);
        return new RefreshReqRes(token);
    }


    /**
     * Resets the password for an unauthenticated user.
     *
     * @param loginRequest user email and replacement password
     */
    @Override
    public void forgotPassword(LoginRequest loginRequest) {
        userService.forgotPassword(loginRequest);
    }

    private String buildKey(String requestId, String purpose) {
        return requestId + ":" + purpose;
    }
}
