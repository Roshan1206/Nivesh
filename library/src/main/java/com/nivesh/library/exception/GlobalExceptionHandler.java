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

    /** Request context used to populate error response metadata. */
    private final WebRequest request;

    /**
     * Creates a handler with the current web request context.
     */
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

    /**
     * Handles OTP validation and verification failures.
     */
    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(OtpException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        HttpStatus status = switch (exception.getErrorCode()) {
            case EXPIRED -> HttpStatus.GONE;
            case INVALID -> HttpStatus.PRECONDITION_FAILED;
            case MAX_ATTEMPTS_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
        };
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handles service unavailable exceptions
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException exception){
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }


    /**
     * Handles session expired exceptions
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpiredException(SessionExpiredException exception){
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
    }


    /**
     * Handles cache not found exceptions
     */
    @ExceptionHandler(CacheNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCacheNotFoundException(CacheNotFoundException exception){
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(response);
    }


    /**
     * Handles service timeout exceptions
     */
    @ExceptionHandler(ServiceTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleServiceTimeoutException(ServiceTimeoutException exception){
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }
}
