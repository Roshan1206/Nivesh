package com.nivesh.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload returned by the authentication API for register response operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    /** Default message returned after successful registration. */
    private static final String WELCOME_MESSAGE = "Welcome to Nivesh.";

    /** Message returned to the client. */
    private String message;

    /** Email address supplied by the client. */
    private String email;

    /** Token payload returned after authentication. */
    private TokenResponse tokens;

    /** Builds a successful registration response with the standard welcome message. */
    public RegisterResponse(String email, TokenResponse tokens) {
        this(WELCOME_MESSAGE, email, tokens);
    }
}
