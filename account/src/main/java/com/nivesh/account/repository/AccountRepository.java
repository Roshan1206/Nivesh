package com.nivesh.account.repository;

import com.nivesh.account.entity.Account;
import com.nivesh.account.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Account a
                JOIN a.product p
                WHERE a.customerNumber = :customerNumber
                AND a.status = :status
                AND p.productCode = :productCode
            ) THEN true ELSE false END
            """)
    boolean existsActiveAccount(@Param("customerNumber") String customerNumber, @Param("status") Status status,
                                @Param("productCode") String productCode);

}
