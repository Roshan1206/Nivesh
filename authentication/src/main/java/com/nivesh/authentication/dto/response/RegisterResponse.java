package com.nivesh.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private static final String WELCOME_MESSAGE = "Welcome to Nivesh.";

    private String message;
    private String email;
    private String accessToken;
    private String refreshToken;

    public RegisterResponse(String email, String accessToken, String refreshToken) {
        this(WELCOME_MESSAGE, email, accessToken, refreshToken);
    }
}
