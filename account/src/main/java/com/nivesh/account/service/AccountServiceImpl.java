package com.nivesh.account.service;

import com.nivesh.account.dto.request.AccountRequest;
import com.nivesh.account.dto.response.AccountResponse;
import com.nivesh.account.entity.Account;
import com.nivesh.account.entity.IdempotencyRecord;
import com.nivesh.account.entity.Product;
import com.nivesh.account.entity.enums.OperationType;
import com.nivesh.account.entity.enums.Status;
import com.nivesh.account.exception.AccountAlreadyExistsException;
import com.nivesh.account.exception.AccountNotFoundException;
import com.nivesh.account.exception.InsufficientBalanceException;
import com.nivesh.account.repository.AccountRepository;
import com.nivesh.account.repository.IdempotencyRepository;
import com.nivesh.library.dto.request.AmountTransactionRequest;
import com.nivesh.library.dto.request.TransactionRequest;
import com.nivesh.library.dto.response.AccountTransactionResponse;
import com.nivesh.library.dto.response.AccountValidationResponse;
import com.nivesh.library.service.SequenceGenerator;
import com.nivesh.library.util.LuhnUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing Accounts.
 *
 * @author Roshan
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    /** Repository used to persist and query accounts. */
    private final AccountRepository accountRepository;

    /** Repository used to store idempotent account operation responses. */
    private final IdempotencyRepository idempotencyRepository;

    /** Generator used to allocate account-number sequences. */
    private final SequenceGenerator sequenceGenerator;

    /** Service used to resolve product configuration for accounts. */
    private final ProductService productService;

    /**
     * Injects repositories and services required to manage accounts.
     */
    public AccountServiceImpl(AccountRepository accountRepository, IdempotencyRepository idempotencyRepository,
                              SequenceGenerator sequenceGenerator, ProductService productService) {
        this.accountRepository = accountRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.sequenceGenerator = sequenceGenerator;
        this.productService = productService;
    }

    /**
     * Loads an account by identifier or raises a not-found exception for the API layer.
     */
    @Override
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found with given account id : " + accountId)
        );
    }

    /**
     * Creates a new active account after enforcing duplicate-account and minimum-balance rules.
     */
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

    /**
     * Retrieves account information and maps it to the external account response contract.
     */
    @Override
    public AccountResponse getAccountInfo(UUID accountId) {
        return buildAccountResponse(getAccount(accountId));
    }

    /**
     * Fetches only the available balance for lightweight balance checks.
     */
    @Override
    public BigDecimal getAvailableBalance(UUID accountId) {
        return getAccount(accountId).getAvailableBalance();
    }

    @Override
    public AccountValidationResponse validateAccount(TransactionRequest request) {
        AccountValidationResponse response = new AccountValidationResponse();
        if (request.getSourceAccountNumber() != null) {
            Optional<Account> account = accountRepository.findByAccountNumber(request.getSourceAccountNumber());
            if (account.isPresent()){
                response.setSourceAccountId(account.get().getId());
                response.setBalanceSufficient(request.getAmount().compareTo(account.get().getAvailableBalance()) < 0);
            } else {
                response.setSourceAccountId(null);
                response.setBalanceSufficient(false);
            }
        }
        if (request.getDestinationAccountNumber() != null) {
            Optional<Account> account = accountRepository.findByAccountNumber(request.getDestinationAccountNumber());
            account.ifPresent(value -> response.setDestinationAccountId(value.getId()));
        }
        return response;
    }

    @Transactional
    @Override
    public AccountTransactionResponse debit(UUID accountId, UUID idempotencyKey, AmountTransactionRequest request) {
        OperationType type = OperationType.DEBIT;
        String newIdempotencyKey = type.name() + ":" + idempotencyKey;
        Optional<IdempotencyRecord> record = idempotencyRepository.findByIdempotencyKey(newIdempotencyKey);
        if (record.isPresent()) {
            return new AccountTransactionResponse(record.get().getResponseStatusCode(), record.get().getRunningBalance());
        }
        Account account = getAccount(accountId);
        BigDecimal availableBalance = account.getAvailableBalance();
        BigDecimal newAvailableBalance = availableBalance;
        BigDecimal amount = request.getAmount();
        if(amount.compareTo(availableBalance) > 0) {
            AccountTransactionResponse conflictResponse = new AccountTransactionResponse(HttpStatus.CONFLICT.value(), availableBalance);
            saveIdempotency(request, accountId, newIdempotencyKey, conflictResponse, type);
            return conflictResponse;
        }
        newAvailableBalance = availableBalance.subtract(amount);
        BigDecimal newBalance = account.getBalance().subtract(amount);

        return getAccountTransactionResponse(accountId, newIdempotencyKey, request, type, account, newAvailableBalance, newBalance);
    }

    @Override
    public AccountTransactionResponse credit(UUID accountId, UUID idempotencyKey, AmountTransactionRequest request) {
        OperationType type = OperationType.CREDIT;
        String newIdempotencyKey = type.name() + ":" + idempotencyKey;
        Optional<IdempotencyRecord> record = idempotencyRepository.findByIdempotencyKey(newIdempotencyKey);
        if (record.isPresent()) {
            return new AccountTransactionResponse(record.get().getResponseStatusCode(), record.get().getRunningBalance());
        }
        Account account = getAccount(accountId);
        BigDecimal availableBalance = account.getAvailableBalance();
        BigDecimal amount = request.getAmount();
        BigDecimal newAvailableBalance = availableBalance.add(amount);
        BigDecimal newBalance = account.getBalance().add(amount);

        return getAccountTransactionResponse(accountId, newIdempotencyKey, request, type, account, newAvailableBalance, newBalance);
    }

    @NonNull
    private AccountTransactionResponse getAccountTransactionResponse(UUID accountId, String idempotencyKey, AmountTransactionRequest request, OperationType type, Account account, BigDecimal newAvailableBalance, BigDecimal newBalance) {
        account.setAvailableBalance(newAvailableBalance);
        account.setBalance(newBalance);

        try {
            accountRepository.save(account);
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConcurrentModificationException("Concurrent modification on account " + accountId + ". Retry required");
        }

        AccountTransactionResponse response = new AccountTransactionResponse(HttpStatus.OK.value(), newAvailableBalance);
        saveIdempotency(request, accountId, idempotencyKey, response, type);
        return response;
    }

    private void saveIdempotency(AmountTransactionRequest request, UUID accountId,
                                 String idempotencyKey, AccountTransactionResponse response, OperationType type) {
        IdempotencyRecord record = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .accountId(accountId)
                .amount(request.getAmount())
                .runningBalance(response.getRunningBalance())
                .type(type)
                .responseStatusCode(response.getStatus())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        idempotencyRepository.save(record);
    }

    /**
     * Maps a persisted account entity into the API response payload.
     */
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

    /**
     * Generates a Luhn-protected account number from the product prefix and product sequence.
     */
    private String generateAccountNumber(Product product) {
        String sequenceName = product.getSequenceName();
        long number = sequenceGenerator.generateNextSeqValue(sequenceName);
        String sequenceNumber = product.getProductPrefix() + String.format("%09d", number);
        int lastDigit = LuhnUtil.computeLastDigit(sequenceNumber);
        return sequenceNumber + lastDigit;
    }
}
