package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.dto.request.ResetPasswordRequest;
import com.nivesh.authentication.entity.Role;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.entity.UserRole;
import com.nivesh.authentication.repository.UserRepository;
import com.nivesh.authentication.service.RoleService;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.authentication.service.UserService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.entity.enums.CustomerStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing users
 *
 * @author Roshan
 */
@Service
public class UserServiceImpl implements UserService {

    /** Encoder used to hash and verify passwords. */
    private final PasswordEncoder passwordEncoder;

    private final RedisTemplate<String, String> redisTemplate;

    /** Service used to manage user roles. */
    private final RoleService roleService;

    /** Service used to issue and refresh tokens. */
    private final TokenService tokenService;

    /** Repository used to persist and query users. */
    private final UserRepository userRepository;

    /**
     * Injects dependencies used to register, load, and update users.
     */
    public UserServiceImpl(PasswordEncoder passwordEncoder, RoleService roleService,
                           TokenService tokenService, UserRepository userRepository, RedisTemplate<String, String> redisTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }


    /**
     * Creates a new user account based on the provided registration details.
     *
     * @param request A RegisterRequest object containing the user's information.
     * @return The newly created User object.
     */
    @Transactional
    @Override
    public User createNewUser(RegisterRequest request) {
        Role role = roleService.getRole("CUSTOMER");

        User user = User.builder()
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .customerStatus(CustomerStatus.ONBOARDED)
                .build();

        user.getUserRoles().add(new UserRole(user, role));
        return userRepository.save(user);
    }


    /**
     * Retrieves a User object from the database based on the provided email address.
     *
     * @param email The email address to search for.
     * @return A User object if found, or null if no user with that email exists.
     */
    @Transactional(readOnly = true)
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailWithRolesAndPermissions(email).orElseThrow(
                () -> new UsernameNotFoundException(email + " not found.")
        );
    }

    /**
     * Updates the user status. Should only be used by other services.
     * Status is responsible for generating different types of access token.
     *
     * @param userId user for the status needs to be updated
     * @param status updated status
     * @return newly access token with refreshed claims
     */
    @Transactional
    @Override
    public String updateStatus(String userId, String status) {
        User user = getUser(userId);
        CustomerStatus customerStatus = CustomerStatus.valueOf(status.toUpperCase());
        String roleName = customerStatus.isEqual(CustomerStatus.REGISTERED) ? "CUSTOMER_REGISTERED" : "CUSTOMER_ACTIVE";
        updateStatus(user, customerStatus);
        return tokenService.generateAccessToken(user, Constants.ACCESS_TOKEN);
    }


    /**
     * Updates the status of a customer.
     *
     * @param user The user making the update.
     * @param customerStatus The new status to apply to the customer.
     */
    @Transactional
    @Override
    public void updateStatus(User user, CustomerStatus customerStatus) {
        user.setCustomerStatus(customerStatus);
        save(user);
    }


    /**
     * Initiates the password reset process by sending a password reset link to the user's registered email address.
     *
     * @param request The LoginRequest object containing the user's email address.
     */
    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = getUserByEmail(request.getEmail());

        if (request.getOldPassword() != null) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BadCredentialsException("Incorrect current password");
            }
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadCredentialsException("New passwords don't match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        save(user);
    }


    /**
     * Saves a User object to the database.
     *
     * @param user The User object to save.
     */
    @Override
    public void save(User user) {
        userRepository.save(user);
    }


    /**
     * Increments the token version for a given user ID.
     *
     * @param userId The ID of the user whose token version should be incremented.
     */
    @Override
    public void incrementTokenVersion(String userId) {
        User user = getUser(userId);
        int tokenVersion = user.getTokenVersion() + 1;
        user.setTokenVersion(tokenVersion);
        redisTemplate.opsForValue().set("tok_ver:" + userId, String.valueOf(tokenVersion));
    }


    /**
     * Retrieves a user from the database based on the provided ID.
     *
     * @param id The unique identifier of the user to retrieve.
     * @return A User object if found, or null if no user with that ID exists.
     */
    @Override
    public User getUser(String id) {
        UUID userId = UUID.fromString(id);
        return userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
    }


    /**
     * Returns true is user exists by email
     *
     * @param email user email
     */
    @Override
    public boolean isUserExistByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    /**
     * Returns true is user exists by email
     *
     * @param mobileNumber user's mobile number
     */
    @Override
    public boolean isUserExistByMobile(String mobileNumber) {
        return userRepository.existsByMobileNumber(mobileNumber);
    }
}
