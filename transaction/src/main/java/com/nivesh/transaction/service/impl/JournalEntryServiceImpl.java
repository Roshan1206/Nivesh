package com.nivesh.transaction.service.impl;

import com.nivesh.library.dto.event.CreditResultEvent;
import com.nivesh.library.dto.event.DebitResultEvent;
import com.nivesh.library.dto.event.TransferRequestedEvent;
import com.nivesh.library.dto.event.TransferResultEvent;
import com.nivesh.transaction.entity.GLAccount;
import com.nivesh.transaction.entity.JournalEntry;
import com.nivesh.transaction.entity.Transaction;
import com.nivesh.transaction.entity.enums.DrCr;
import com.nivesh.transaction.entity.enums.TransactionStatus;
import com.nivesh.transaction.exception.JournalEntryException;
import com.nivesh.transaction.repository.JournalEntryRepository;
import com.nivesh.transaction.service.JournalEntryService;
import com.nivesh.transaction.service.TransactionService;
import com.nivesh.transaction.service.TransactionTypeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class JournalEntryServiceImpl implements JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    private final TransactionService transactionService;

    private final TransactionTypeConfigService configService;

    public JournalEntryServiceImpl(JournalEntryRepository journalEntryRepository, TransactionService transactionService,
                                   TransactionTypeConfigService configService) {
        this.journalEntryRepository = journalEntryRepository;
        this.transactionService = transactionService;
        this.configService = configService;
    }

    @Override
    public void writeLedger(TransferResultEvent event) {
        TransferRequestedEvent requestedEvent = event.getTransferRequest();
        String referenceNumber = requestedEvent.getReferenceNumber();
        Transaction txn = transactionService.getTransactionByRefNo(referenceNumber);

        if (txn.getStatus() == TransactionStatus.POSTED) {
            log.warn("Transaction already posted. Reference number: {}", txn.getReferenceNumber());
            return;
        }

        int existingCount = journalEntryRepository.countByTransaction_Id(txn.getId());
        if (existingCount > 0) {
            throw new JournalEntryException("Journal entries already exist for txnId=" + txn.getId() +
                    ". count=" + existingCount + ". Possible duplicate event");
        }

        GLAccount glAccount = txn.getTypeConfig().getGlAccount();
        Instant postedAt = Instant.now();

        JournalEntry debitEntry = JournalEntry.builder()
                .transaction(txn)
                .accountId(requestedEvent.getSourceAccountId())
                .glAccountId(glAccount)
                .drCr(DrCr.DR)
                .amount(requestedEvent.getAmount())
                .runningBalance(event.getPostDebitBalance())
                .narration(buildDescription("DR", txn.getDescription()))
                .postedAt(postedAt)
                .build();

        JournalEntry creditEntry = JournalEntry.builder()
                .transaction(txn)
                .accountId(requestedEvent.getDestinationAccountId())
                .glAccountId(glAccount)
                .drCr(DrCr.CR)
                .amount(requestedEvent.getAmount())
                .runningBalance(event.getPostCreditBalance())
                .narration(buildDescription("CR", txn.getDescription()))
                .postedAt(postedAt)
                .build();

        journalEntryRepository.saveAll(List.of(debitEntry, creditEntry));

    }


    @Override
    public void writeLedger(DebitResultEvent event) {

    }

    @Override
    public void writeLedger(CreditResultEvent event) {

    }

    private String buildDescription(String cr, String description) {
        if (description != null && !description.isBlank()) {
            return description;
        }
        return cr.equals("DR")
                ? "Debit"
                : "Credit";
    }
}
