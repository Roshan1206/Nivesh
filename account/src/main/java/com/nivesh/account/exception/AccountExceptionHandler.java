package com.nivesh.account.exception;

import com.nivesh.library.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Centralized exception handler for translating account errors into HTTP responses.
 */
@ControllerAdvice
public class AccountExceptionHandler {

    /**
     * Handles duplicate-account creation requests.
     */
    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAccountAlreadyExistsException(AccountAlreadyExistsException exception,
                                                                             WebRequest request) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    /**
     * Handles account lookup failures.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException exception,
                                                                        WebRequest request) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    /**
     * Handles account operations rejected for insufficient balance.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException exception,
                                                                            WebRequest request) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    /**
     * Handles product lookup failures.
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException exception,
                                                                        WebRequest request) {
        ErrorResponse response = new ErrorResponse(exception.getMessage(), request);
        return ResponseEntity.status(exception.getStatus()).body(response);
    }
}
