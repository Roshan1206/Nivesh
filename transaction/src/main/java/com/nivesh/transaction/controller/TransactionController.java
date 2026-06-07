package com.nivesh.transaction.controller;

import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.transaction.dto.response.TransactionResponse;
import com.nivesh.transaction.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller that exposes transaction API endpoints for transaction operations.
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    /** Service that processes transaction requests. */
    private final TransactionService transactionService;

    /**
     * Injects the transaction service used by transaction endpoints.
     */
    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }


    /**
     * Initiates a new transaction with an OTP response.
     *
     * @param idempotencyKey The idempotency key for the transaction.
     * @param request The transaction request containing the details of the transaction.
     * @return A ResponseEntity containing the OTP response for the initiated transaction.
     */
    @PostMapping
    public ResponseEntity<OtpResponse> initiateTransaction(@RequestHeader String idempotencyKey, @RequestBody TransactionRequest request) {
        OtpResponse otpResponse = transactionService.initiateTransaction(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(otpResponse);
    }


    /**
     * Starts a new transaction with the provided request ID and OTP.
     *
     * @param requestId The unique identifier for the transaction request.
     * @param otp The one-time password used to authenticate the transaction.
     * @return A ResponseEntity containing the TransactionResponse object upon successful transaction start.
     */
    @PostMapping(value = "/{requestId}/verify", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<TransactionResponse> startTransaction(@PathVariable String requestId,  @RequestBody String otp) {
        TransactionResponse response = transactionService.startTransaction(requestId, otp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
