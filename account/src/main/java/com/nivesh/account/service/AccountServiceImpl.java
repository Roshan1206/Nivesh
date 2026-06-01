package com.nivesh.account.service;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.entity.Account;
import com.nivesh.account.entity.Product;
import com.nivesh.account.entity.enums.Status;
import com.nivesh.account.exception.AccountAlreadyExistsException;
import com.nivesh.account.exception.AccountNotFoundException;
import com.nivesh.account.exception.InsufficientBalanceException;
import com.nivesh.account.repository.AccountRepository;
import com.nivesh.library.service.SequenceGenerator;
import com.nivesh.library.util.LuhnUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service class for managing Accounts.
 *
 * @author Roshan
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final SequenceGenerator sequenceGenerator;

    private final ProductService productService;

    public AccountServiceImpl(AccountRepository accountRepository, SequenceGenerator sequenceGenerator, ProductService productService) {
        this.accountRepository = accountRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.productService = productService;
    }


    @Override
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found with given account id : " + accountId)
        );
    }

    @Override
    public AccountResponse createAccount(AccountRequest accountRequest) {
        String customerNumber = accountRequest.getCustomerNumber();
        String productCode = accountRequest.getProductCode();
        if (accountRepository.existsActiveAccount(customerNumber,
                Status.ACTIVE, productCode)) {
            throw new AccountAlreadyExistsException(HttpStatus.CONFLICT, "Account already with selected product");
        }

        Product product = productService.getProduct(productCode);
        BigDecimal balance = accountRequest.getOpeningBalance();

        if (balance.compareTo(product.getMinBalance()) < 0) {
            throw new InsufficientBalanceException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Selected product needed a minimum of " + accountRequest.getOpeningBalance() + " to open account");
        }

        String accountNumber = generateAccountNumber(product);
        Account account = new Account(accountNumber, customerNumber, balance, product);
        accountRepository.save(account);
        return buildAccountResponse(account);
    }


    @Override
    public AccountResponse getAccountInfo(UUID accountId) {
        return buildAccountResponse(getAccount(accountId));
    }

    @Override
    public BigDecimal getAvailableBalance(UUID accountId) {
        return getAccount(accountId).getAvailableBalance();
    }


    private static AccountResponse buildAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setCustomerNumber(account.getCustomerNumber());
        response.setStatus(account.getStatus());
        response.setAvailableBalance(account.getAvailableBalance());
        response.setProductCode(account.getProduct().getProductCode());
        response.setProductName(account.getProduct().getProductName());
        return response;
    }

    private String generateAccountNumber(Product product) {
        String sequenceName = product.getSequenceName();
        long number = sequenceGenerator.generateNextSeqValue(sequenceName);
        String sequenceNumber = product.getProductPrefix() + String.format("%09d", number);
        int lastDigit = LuhnUtil.computeLastDigit(sequenceNumber);
        return sequenceNumber + lastDigit;
    }
}