package com.vk42.cbp.firstmodule.outbox.listener;

import com.vk42.cbp.firstmodule.outbox.domain.OutboxEvent;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEventRepository;
import com.vk42.cbp.firstmodule.payments.events.PaymentStatusChangedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class OutboxEventListener {
    private final OutboxEventRepository eventRepository;

    public OutboxEventListener(OutboxEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processCreatedEvent(PaymentStatusChangedEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("PaymentIntent");
        outboxEvent.setAggregateId(event.paymentId().toString());
        outboxEvent.setEventType("PAYMENT_" + event.newState());
        outboxEvent.setPayload(event.json());
        this.eventRepository.save(outboxEvent);
    }
}
