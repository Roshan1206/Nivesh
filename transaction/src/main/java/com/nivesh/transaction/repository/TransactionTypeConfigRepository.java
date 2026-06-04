package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.TransactionTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeConfigRepository extends JpaRepository<TransactionTypeConfig, String> {
}
