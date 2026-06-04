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

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<OtpResponse> initiateTransaction(@RequestHeader String idempotencyKey, @RequestBody TransactionRequest request) {
        OtpResponse otpResponse = transactionService.initiateTransaction(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(otpResponse);
    }

    @PostMapping(value = "/{requestId}/verify", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<TransactionResponse> startTransaction(@PathVariable String requestId,  @RequestBody String otp) {
        TransactionResponse response = transactionService.startTransaction(requestId, otp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
