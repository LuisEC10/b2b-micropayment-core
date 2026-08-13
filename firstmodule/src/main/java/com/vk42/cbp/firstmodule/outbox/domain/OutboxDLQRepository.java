package com.vk42.cbp.firstmodule.outbox.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxDLQRepository extends JpaRepository<OutboxDLQ, UUID> {
}
