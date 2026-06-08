package com.nivesh.authentication.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    private String email;

    /** Old Password supplied by the client. */
    private String oldPassword;

    /** New Password supplied by the client. */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Password must be of min 8 length, 1 Capital letter, 1 small letter, 1 Special character")
    private String newPassword;

    /** Confirm new Password supplied by the client. */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
            message = "Password must be of min 8 length, 1 Capital letter, 1 small letter, 1 Special character")
    private String confirmNewPassword;
}
