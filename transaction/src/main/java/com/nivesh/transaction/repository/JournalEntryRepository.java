package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for persisting and querying journal entry records.
 */
@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
}
