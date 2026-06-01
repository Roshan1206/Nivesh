package com.nivesh.account.controller;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Controller class for Accounts.
 *
 * @author Roshan
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createNewAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable UUID accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccountInfo(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable UUID accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAvailableBalance(accountId));
    }
}
