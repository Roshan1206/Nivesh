package com.nivesh.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO class used for user registration
 *
 * @author Roshan
 */
@Data
public class RegisterRequest {

    /** Email address supplied by the client. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /** Mobile number returned to or supplied by the client. */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone number should be of 10 digits")
    private String mobileNumber;

    /** Password supplied by the client. */
    @NotBlank(message = "Password can not be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Password should be minimum 8 character and include atleast 1 Capital letter, 1 Small letter, 1 digit & 1 special character")
    private String password;
}
