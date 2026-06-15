package com.nivesh.transaction.service;

import com.nivesh.library.dto.event.CreditResultEvent;
import com.nivesh.library.dto.event.DebitResultEvent;
import com.nivesh.library.dto.event.TransferResultEvent;

public interface JournalEntryService {

    void writeLedger(TransferResultEvent event);

    void writeLedger(DebitResultEvent event);

    void writeLedger(CreditResultEvent event);
}
