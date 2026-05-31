package com.nivesh.account.service;

import com.nivesh.account.dto.response.AccountResponse;

/**
 * Interface for managing Account.
 *
 * @author Roshan
 */
public interface AccountService {

    AccountResponse getAccountBalance(String accountNumber);

}
