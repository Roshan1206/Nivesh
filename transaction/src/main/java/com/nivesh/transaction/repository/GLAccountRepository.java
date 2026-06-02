package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.GLAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GLAccountRepository extends JpaRepository<GLAccount, UUID> {
}
