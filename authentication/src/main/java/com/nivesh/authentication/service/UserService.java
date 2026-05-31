package com.nivesh.authentication.service;

import com.nivesh.authentication.dto.request.LoginRequest;
import com.nivesh.authentication.dto.request.RegisterRequest;
import com.nivesh.authentication.entity.User;
import com.nivesh.library.entity.enums.CustomerStatus;

/**
 * Interface for managing users.
 *
 * @author Roshan
 */
public interface UserService {

    User createNewUser(RegisterRequest request);

    User getUserByEmail(String email);

    String updateStatus(String userId, String status);

    void updateStatus(User user, CustomerStatus customerStatus);

    void forgotPassword(LoginRequest request);

    void save(User user);
}
