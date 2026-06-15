package com.nivesh.transaction.repository;

import com.nivesh.transaction.entity.OutboxEvent;
import com.nivesh.transaction.entity.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByOutboxStatusAndCreatedAtBefore(OutboxStatus outboxStatus, Instant createdAt);

    Optional<OutboxEvent> findByAggregateId(String aggregateId);

    long countByOutboxStatus(OutboxStatus outboxStatus);

    @Query("""
        UPDATE OutboxEvent o
        SET o.outboxStatus = 'FAILED'
        WHERE o.eventId IN :eventIds
    """)
    void markAsFailed(@Param("eventIds") List<UUID> eventIds);
}
