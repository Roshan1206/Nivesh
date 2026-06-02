package com.nivesh.account.service;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Interface for managing Account.
 *
 * @author Roshan
 */
public interface AccountService {

    /** Returns the account entity for the supplied identifier. */
    Account getAccount(UUID accountId);

    /** Creates a new account and returns the response DTO for the created record. */
    AccountResponse createAccount(AccountRequest accountRequest);

    /** Builds account details for API consumers. */
    AccountResponse getAccountInfo(UUID accountId);

    /** Returns the available balance for balance-only account lookups. */
    BigDecimal getAvailableBalance(UUID accountId);

}
