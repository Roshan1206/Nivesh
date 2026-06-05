package com.nivesh.customer.repository;

import com.nivesh.customer.entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for persisting and querying KYC document records.
 */
@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
}
