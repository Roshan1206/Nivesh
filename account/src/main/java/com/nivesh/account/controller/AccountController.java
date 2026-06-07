package com.nivesh.account.controller;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.service.AccountService;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountTransactionResponse;
import com.nivesh.library.dto.response.AccountValidationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

import static com.nivesh.library.constant.Constants.IDEMPOTENCY_KEY;

/**
 * Controller class for Accounts.
 *
 * @author Roshan
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    /** Service used to process account API requests. */
    private final AccountService accountService;

    /**
     * Injects the account service used by account endpoints.
     */
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    /**
     * Opens a new account for a customer using the requested product code and opening balance.
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createNewAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    /**
     * Returns account profile details for the supplied account identifier.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable UUID accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccountInfo(accountId));
    }

    /**
     * Returns the currently available balance for the supplied account identifier.
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAvailableBalance(accountId));
    }


    /**
     * Validates an account based on the provided transaction request and returns a response indicating success or failure.
     *
     * @param request The TransactionRequest object containing the details of the transaction to validate.
     * @return A ResponseEntity containing an AccountValidationResponse object, which will contain validation results.
     */
    @PostMapping("/internal/validate")
    public ResponseEntity<AccountValidationResponse> validateAccount(@RequestBody TransactionRequest request) {
        AccountValidationResponse response = accountService.validateAccount(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * Debits the specified account and returns the transaction response.
     *
     * @param idempotencyKey The idempotency key for the request.
     * @param accountId The ID of the account to debit.
     * @param request The amount transaction request containing the debit amount.
     * @return A ResponseEntity containing the AccountTransactionResponse representing the debit operation.
     */
    @PostMapping("/internal/{accountId}/debit")
    public ResponseEntity<AccountTransactionResponse> debitAccount(@RequestHeader String idempotencyKey,
                                                                   @PathVariable UUID accountId,
                                                                   @RequestBody AmountTransactionRequest request) {
        AccountTransactionResponse debit = accountService.debit(accountId, UUID.fromString(idempotencyKey), request);
        return ResponseEntity.status(debit.getStatus()).body(debit);
    }


    /**
     * Credits the specified account with the transaction details provided in the request.
     *
     * @param idempotencyKey The idempotency key for this request.
     * @param accountId The ID of the account to credit.
     * @param request The AmountTransactionRequest containing the transaction details.
     * @return A ResponseEntity containing the AccountTransactionResponse representing the successful credit operation.
     */
    @PostMapping("/internal/{accountId}/credit")
    public ResponseEntity<AccountTransactionResponse> creditAccount(@RequestHeader String idempotencyKey,
                                                                    @PathVariable UUID accountId,
                                                                    @RequestBody AmountTransactionRequest request) {
        AccountTransactionResponse credit = accountService.credit(accountId, UUID.fromString(idempotencyKey), request);
        return ResponseEntity.status(credit.getStatus()).body(credit);
    }
}
