package com.vk42.cbp.firstmodule.payments.api;

import com.vk42.cbp.firstmodule.payments.domain.PaymentState;
import com.vk42.cbp.firstmodule.payments.service.PaymentService;
import com.vk42.cbp.firstmodule.security.jws.WorkerSignatureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    final PaymentService paymentService;
    final WorkerSignatureService worker;

    public PaymentController(PaymentService paymentService, WorkerSignatureService worker) {
        this.paymentService = paymentService;
        this.worker = worker;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentInitializationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.paymentService.initializePayment(request));
    }

    @PostMapping("/webhooks")
    public ResponseEntity<?> webhookMotor(@Valid @RequestBody WebhookPayload payload) {
        return ResponseEntity.ok(this.paymentService.processWebhooks(payload));
    }

    @GetMapping("/{paymentId}")
    public PaymentState getCurrentPaymentState(@PathVariable Long paymentId) {
        return this.paymentService.currentPaymentState(paymentId);
    }

    @GetMapping("/test-signature")
    public ResponseEntity<?> verifySignature() {
        return ResponseEntity.ok(this.worker.generateJwsForPayment("123", "CONFIRMED"));
    }
}
