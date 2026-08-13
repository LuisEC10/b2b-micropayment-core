package com.vk42.cbp.firstmodule.repositories;

import com.vk42.cbp.firstmodule.entities.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
}
