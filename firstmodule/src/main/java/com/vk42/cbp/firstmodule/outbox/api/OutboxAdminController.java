package com.vk42.cbp.firstmodule.outbox.api;

import com.vk42.cbp.firstmodule.outbox.domain.OutboxDLQ;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxDLQRepository;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEvent;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/outbox/dlq")
public class OutboxAdminController {

    private final OutboxDLQRepository dlqRepository;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxAdminController(OutboxDLQRepository dlqRepository, OutboxEventRepository outboxEventRepository) {
        this.dlqRepository = dlqRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @GetMapping
    public ResponseEntity<List<OutboxDLQ>> getAllDLQEvents() {
        return ResponseEntity.ok(this.dlqRepository.findAll());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryDlqEvent(@PathVariable UUID id) {
        Optional<OutboxDLQ> dlpOptional = this.dlqRepository.findById(id);

        if (dlpOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OutboxDLQ dlq = dlpOptional.orElseThrow();

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(dlq.getAggregateType());
        event.setAggregateId(dlq.getAggregateId());
        event.setEventType(dlq.getEventType());
        event.setPayload(dlq.getPayload());
        event.setRetryCount(0);
        event.setCreatedAt(LocalDateTime.now());
        event.setNextRetryAt(Instant.now());

        this.outboxEventRepository.save(event);

        this.dlqRepository.delete(dlq);

        return ResponseEntity.ok("Event" + event.getId() + " re-send to main queue");
    }
}