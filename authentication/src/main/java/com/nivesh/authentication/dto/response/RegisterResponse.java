package com.nivesh.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    /** Default message returned after successful registration. */
    private static final String WELCOME_MESSAGE = "Welcome to Nivesh.";

    private String message;
    private String email;
    private TokenResponse tokens;

    /** Builds a successful registration response with the standard welcome message. */
    public RegisterResponse(String email, TokenResponse tokens) {
        this(WELCOME_MESSAGE, email, tokens);
    }
}
