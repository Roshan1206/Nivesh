package com.nivesh.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshReqRes {

    /** Refresh token supplied by the client or replacement access token returned by the API. */
    @NotBlank(message = "Token not found.")
    private String token;
}
