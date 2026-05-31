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
    private TokenResponse tokens;

    public RegisterResponse(String email, TokenResponse tokens) {
        this(WELCOME_MESSAGE, email, tokens);
    }
}
