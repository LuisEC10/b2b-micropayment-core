package com.vk42.cbp.firstmodule.payments.service;

import com.vk42.cbp.firstmodule.payments.api.PaymentInitializationRequest;
import com.vk42.cbp.firstmodule.payments.api.WebhookPayload;
import com.vk42.cbp.firstmodule.payments.api.WebhookResponse;
import com.vk42.cbp.firstmodule.payments.domain.IdempotencyKeyRecord;
import com.vk42.cbp.firstmodule.payments.domain.IdempotencyKeyRecordRepository;
import com.vk42.cbp.firstmodule.payments.domain.PaymentIntent;
import com.vk42.cbp.firstmodule.payments.domain.PaymentIntentRepository;
import com.vk42.cbp.firstmodule.payments.domain.PaymentState;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEvent;
import com.vk42.cbp.firstmodule.outbox.domain.OutboxEventRepository;
import com.vk42.cbp.firstmodule.shared.exceptions.IllegalStateTransactionException;
import com.vk42.cbp.firstmodule.shared.exceptions.PaymentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{

    final IdempotencyKeyRecordRepository idempotencyKeyRecordRepository;
    final PaymentIntentRepository paymentIntentRepository;
    final OutboxEventRepository outboxEventRepository;
    final ObjectMapper objectMapper;

    public PaymentServiceImpl(
            IdempotencyKeyRecordRepository idempotencyKeyRecordRepository,
            PaymentIntentRepository paymentIntentRepository, ObjectMapper objectMapper,
            OutboxEventRepository outboxEventRepository) {
        this.idempotencyKeyRecordRepository = idempotencyKeyRecordRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.objectMapper = objectMapper;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    @Transactional
    public String initializePayment(PaymentInitializationRequest request) {
        Optional<IdempotencyKeyRecord> idempotencyKeyRecord = this.idempotencyKeyRecordRepository.findById(request.idempotencyKey());
        if (idempotencyKeyRecord.isPresent()) {
            return idempotencyKeyRecord.get().getData();
        }

        PaymentIntent newPay = new PaymentIntent();
        newPay.setAmount(request.amount());
        newPay.setPaymentState(PaymentState.CREATED);
        newPay.setIdempotencyKey(request.idempotencyKey());
        newPay.setCurrency(request.currency());

        this.paymentIntentRepository.save(newPay);

        WebhookResponse response = new WebhookResponse("SUCCESS", newPay.getId());
        String jsonString;
        try {
            jsonString = this.objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Error: Webhook Response Serialization Gone Wrong");
        }

        IdempotencyKeyRecord newIKR = new IdempotencyKeyRecord();
        newIKR.setId(request.idempotencyKey());
        newIKR.setData(jsonString);
        newIKR.setHttpStatus(201);

        this.idempotencyKeyRecordRepository.save(newIKR);

        return jsonString;
    }

    @Override
    @Transactional
    public String processWebhooks(WebhookPayload payload) {
        Optional<IdempotencyKeyRecord> idempotencyKeyRecord = this.idempotencyKeyRecordRepository.findById(payload.idempotencyKey());
        if (idempotencyKeyRecord.isPresent()) {
           return idempotencyKeyRecord.get().getData();
        }

        Optional<PaymentIntent> paymentIntent = this.paymentIntentRepository.findById(payload.paymentId());
        if (paymentIntent.isEmpty()) {
            throw new PaymentNotFoundException("Payment Intent Not Found");
        }

        if (!paymentIntent.get().getPaymentState().canTransitionTo(payload.newState())) {
            throw new IllegalStateTransactionException("Transaction not permitted from " +
                    paymentIntent.get().getPaymentState() + " to " + payload.newState());
        }

        PaymentIntent paymentIntentDb = paymentIntent.get();
        IdempotencyKeyRecord newIKR = new IdempotencyKeyRecord();

        WebhookResponse response = new WebhookResponse("SUCCESS", paymentIntentDb.getId());
        String jsonString;
        try {
            jsonString = this.objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Error: Webhook Response Serialization Gone Wrong");
        }

        paymentIntentDb.setPaymentState(payload.newState());
        newIKR.setId(payload.idempotencyKey());
        newIKR.setData(jsonString);
        newIKR.setHttpStatus(200);

        this.paymentIntentRepository.save(paymentIntentDb);
        // Chaos Simulation
        //if ("CRASH_TEST".equals(payload.idempotencyKey())) {
        //    throw new RuntimeException("Simulation: server crashed before final commit");
        //}

        // Create the message for outbox
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("PaymentIntent");
        event.setAggregateId(paymentIntentDb.getId().toString());
        event.setEventType("PAYMENT_" + payload.newState());
        event.setPayload(jsonString);
        // save the message
        this.outboxEventRepository.save(event);

        this.idempotencyKeyRecordRepository.save(newIKR);
        return jsonString;
    }

    @Override
    public PaymentState currentPaymentState(Long paymentId) {
        Optional<PaymentIntent> paymentIntent = this.paymentIntentRepository.findById(paymentId);
        if (paymentIntent.isEmpty()) {
            throw new PaymentNotFoundException("Payment Intent not Found");
        }

        return paymentIntent.get().getPaymentState();
    }
}
