package com.vk42.cbp.firstmodule.outbox.worker;

import com.vk42.cbp.firstmodule.outbox.domain.OutboxDLQ;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxDLQRepository;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEvent;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEventRepository;
import com.vk42.cbp.firstmodule.security.jws.WorkerSignatureService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxProcessorWorker {
    private final OutboxEventRepository outboxRepo;
    private final WorkerSignatureService worker;
    private final OutboxDLQRepository dlqRepo;

    public OutboxProcessorWorker(OutboxEventRepository outboxRepo, WorkerSignatureService worker, OutboxDLQRepository dlqRepo) {
        this.outboxRepo = outboxRepo;
        this.worker = worker;
        this.dlqRepo = dlqRepo;
    }

    @Scheduled(fixedRate = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> outboxEventList = this.outboxRepo.findEligibleEvents(
                Instant.now(),
                PageRequest.of(0, 50)
        );
        outboxEventList.forEach(event -> {
            try {
                String id = event.getAggregateId();
                String payload = event.getPayload();
                String token = this.worker.generateJwsForPayment(id, payload);
                System.out.println("==========================");
                System.out.println("SENDING TO ETHEREUM BLOCKCHAIN");
                System.out.println("TOKEN SENT: " + token);
                System.out.println("==========================");
                this.outboxRepo.delete(event);
            } catch (Exception e) {
                int nextRetry = event.getRetryCount() + 1;
                event.setRetryCount(nextRetry);
                event.setLastError(e.getMessage());

                if (event.getRetryCount() >= event.getMaxRetries()) {
                    OutboxDLQ dlq = new OutboxDLQ();
                    dlq.setOriginalEventId(event.getId());
                    dlq.setAggregateType(event.getAggregateType());
                    dlq.setAggregateId(event.getAggregateId());
                    dlq.setEventType(event.getEventType());
                    dlq.setPayload(event.getPayload());
                    dlq.setFailedAt(LocalDateTime.now());
                    dlq.setReason(e.getMessage());

                    this.dlqRepo.save(dlq);
                    this.outboxRepo.delete(event);
                    System.out.println("DLQ: Max Retries executed. Moving outbox to dlq with Id: " + event.getId());
                    return;
                }

                long secondsToWait = (long) (Math.pow(2, event.getRetryCount()) * 5);
                event.setNextRetryAt(Instant.now().plusSeconds(secondsToWait));
                this.outboxRepo.save(event);
                System.out.println("RETRY: Error processing outbox with Id: " + event.getId() + " - Retrying");
            }
        });
    }

}
