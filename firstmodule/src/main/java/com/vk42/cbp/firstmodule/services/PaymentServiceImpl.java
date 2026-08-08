package com.vk42.cbp.firstmodule.services;

import com.vk42.cbp.firstmodule.dto.PaymentInitializationRequest;
import com.vk42.cbp.firstmodule.dto.WebhookPayload;
import com.vk42.cbp.firstmodule.dto.WebhookResponse;
import com.vk42.cbp.firstmodule.entities.IdempotencyKeyRecord;
import com.vk42.cbp.firstmodule.entities.PaymentIntent;
import com.vk42.cbp.firstmodule.entities.PaymentState;
import com.vk42.cbp.firstmodule.exceptions.IllegalStateTransactionException;
import com.vk42.cbp.firstmodule.exceptions.PaymentNotFoundException;
import com.vk42.cbp.firstmodule.repositories.IdempotencyKeyRecordRepository;
import com.vk42.cbp.firstmodule.repositories.PaymentIntentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService{

    final IdempotencyKeyRecordRepository idempotencyKeyRecordRepository;
    final PaymentIntentRepository paymentIntentRepository;
    final ObjectMapper objectMapper;

    public PaymentServiceImpl(
            IdempotencyKeyRecordRepository idempotencyKeyRecordRepository,
            PaymentIntentRepository paymentIntentRepository, ObjectMapper objectMapper) {
        this.idempotencyKeyRecordRepository = idempotencyKeyRecordRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.objectMapper = objectMapper;
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

        if (!paymentIntent.get().getPaymentState().canTransactionTo(payload.newState())) {
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
