package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for persisting and querying transaction records.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Modifying
//        AND t.createdAt <= :threshold
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.creditRetryCount <= :maxRetryCount
        AND t.status IN ('DEBIT_SUCCESS', 'CREDIT_RETRY')
    """)
    List<Transaction> findStuckCreditTransaction(@Param("threshold") LocalDateTime threshold,
                                                 @Param("maxRetryCount") int maxRetryCount);

    @Modifying
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.creditRetryCount <= :maxRetryCount
        AND t.createdAt <= :threshold
        AND t.status IN ('COMPENSATE_INITIATED')
    """)
    List<Transaction> findStuckCompensateTransaction(@Param("threshold") LocalDateTime threshold,
                                                 @Param("maxRetryCount") int maxRetryCount);
}
