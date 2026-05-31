package com.nivesh.authentication.service.impl;

import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.entity.Role;
import com.nivesh.authentication.entity.User;
import com.nivesh.authentication.entity.UserRole;
import com.nivesh.authentication.repository.UserRepository;
import com.nivesh.authentication.service.RoleService;
import com.nivesh.authentication.service.TokenService;
import com.nivesh.authentication.service.UserService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.entity.enums.CustomerStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service class for managing users
 *
 * @author Roshan
 */
@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    private final TokenService tokenService;

    private final UserRepository userRepository;

    public UserServiceImpl(PasswordEncoder passwordEncoder, RoleService roleService,
                           TokenService tokenService, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

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

    @Transactional
    @Override
    public void updateStatus(User user, CustomerStatus customerStatus) {
        user.setCustomerStatus(customerStatus);
        save(user);
    }

    @Transactional
    @Override
    public void forgotPassword(LoginRequest request) {
        User user = getUserByEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        save(user);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    private User getUser(String id) {
        UUID userId = UUID.fromString(id);
        return userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
    }
}
