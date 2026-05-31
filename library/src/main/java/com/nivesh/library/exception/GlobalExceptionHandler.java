package com.nivesh.library.exception;

import com.nivesh.library.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global exception handler for handling exception across different services.
 *
 * @author Roshan
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final WebRequest request;

    public GlobalExceptionHandler(WebRequest request) {
        this.request = request;
    }

    /**
     * Handles validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(OtpException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        HttpStatus status = switch (exception.getErrorCode()) {
            case EXPIRED -> HttpStatus.GONE;
            case INVALID -> HttpStatus.UNAUTHORIZED;
            case MAX_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
        };
        return ResponseEntity.status(status).body(response);
    }
}
