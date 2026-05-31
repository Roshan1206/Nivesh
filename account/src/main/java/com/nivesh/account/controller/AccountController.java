package com.nivesh.account.controller;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

//  TODO: To be implemented after branch and customer
    @PostMapping("/")
    public ResponseEntity<AccountResponse> createNewAccount(@Valid @RequestHeader AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new AccountResponse());
    }

    @GetMapping("/{accountNumber")
    public ResponseEntity<AccountResponse> getAccountDetails(@Pattern(regexp = "^\\d{11}$", message = "Account number should be of 11 digits")
                                                                 @PathVariable String accountNumber) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new AccountResponse());
    }
}
