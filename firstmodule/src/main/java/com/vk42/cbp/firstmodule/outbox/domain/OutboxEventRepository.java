package com.vk42.cbp.firstmodule.outbox.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query("SELECT e FROM OutboxEvent e WHERE (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now) AND e.retryCount < e.maxRetries ORDER BY e.createdAt ASC")
    List<OutboxEvent> findEligibleEvents(@Param("now") Instant now, Pageable pageable);
}
