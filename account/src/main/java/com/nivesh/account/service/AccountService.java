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

    Account getAccount(UUID accountId);

    AccountResponse createAccount(AccountRequest accountRequest);

    AccountResponse getAccountInfo(UUID accountId);

    BigDecimal getAvailableBalance(UUID accountId);

}