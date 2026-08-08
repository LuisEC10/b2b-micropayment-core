package com.vk42.cbp.firstmodule.repositories;

import com.vk42.cbp.firstmodule.entities.IdempotencyKeyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRecordRepository extends JpaRepository<IdempotencyKeyRecord, String> {
}
