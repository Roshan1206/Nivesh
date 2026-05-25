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

    /**
     * Handles validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(Exception exception, WebRequest request){
        ErrorResponse response = new ErrorResponse();
        response.setPath(request.getDescription(false));
        response.setMessage(exception.getMessage());
        response.setTime(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
