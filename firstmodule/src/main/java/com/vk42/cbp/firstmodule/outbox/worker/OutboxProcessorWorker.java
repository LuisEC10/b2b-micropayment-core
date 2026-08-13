package com.vk42.cbp.firstmodule.outbox.worker;

import com.vk42.cbp.firstmodule.outbox.domain.OutboxEvent;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEventRepository;
import com.vk42.cbp.firstmodule.security.jws.WorkerSignatureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OutboxProcessorWorker {
    final OutboxEventRepository outboxRepo;
    final WorkerSignatureService worker;

    public OutboxProcessorWorker(OutboxEventRepository outboxRepo, WorkerSignatureService worker) {
        this.outboxRepo = outboxRepo;
        this.worker = worker;
    }

    @Scheduled(fixedRate = 5000)
    public void processOutboxEvents() {
        List<OutboxEvent> outboxEventList = this.outboxRepo.findTop50ByOrderByCreatedAtAsc();
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
                System.out.println("ERROR: Error processing outbox with Id: " + event.getId());
            }
        });
    }

}
