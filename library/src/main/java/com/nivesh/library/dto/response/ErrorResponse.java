package com.nivesh.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * DTO class for exceptions. Should be used for handling exceptions.
 *
 * @author Roshan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String path;
    private String message;
    private LocalDateTime time;

    public ErrorResponse(String message, WebRequest request) {
        this.path = request.getDescription(false);
        this.message = message;
        this.time = LocalDateTime.now();
    }
}
