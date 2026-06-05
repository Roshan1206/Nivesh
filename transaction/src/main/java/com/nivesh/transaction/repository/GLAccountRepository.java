package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for persisting and querying g l account records.
 */
@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, UUID> {
}
