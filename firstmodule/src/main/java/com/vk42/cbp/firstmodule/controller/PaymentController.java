package com.vk42.cbp.firstmodule.controller;

import com.vk42.cbp.firstmodule.dto.PaymentInitializationRequest;
import com.vk42.cbp.firstmodule.dto.WebhookPayload;
import com.vk42.cbp.firstmodule.entities.PaymentState;
import com.vk42.cbp.firstmodule.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentInitializationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.paymentService.initializePayment(request));
    }

    @PostMapping("/webhooks")
    public ResponseEntity<?> webhookMotor(@RequestBody WebhookPayload payload) {
        return ResponseEntity.ok(this.paymentService.processWebhooks(payload));
    }

    @GetMapping("/{paymentId}")
    public PaymentState getCurrentPaymentState(@PathVariable Long paymentId) {
        return this.paymentService.currentPaymentState(paymentId);
    }
}
