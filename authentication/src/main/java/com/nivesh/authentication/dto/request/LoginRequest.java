package com.nivesh.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for user login.
 *
 * @author Roshan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /** Email address supplied by the client. */
    @NotBlank(message = "Invalid email or password")
    @Email(message = "Invalid email or password")
    private String email;

    /** Password supplied by the client. */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Invalid email or password")
    private String password;
}
