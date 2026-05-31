package com.nivesh.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT used to authorize protected resource requests. */
    private String accessToken;

    /** Longer-lived JWT used to request a fresh access token. */
    private String refreshToken;
}
