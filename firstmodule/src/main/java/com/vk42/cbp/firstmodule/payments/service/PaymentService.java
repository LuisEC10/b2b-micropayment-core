package com.vk42.cbp.firstmodule.payments.service;

import com.vk42.cbp.firstmodule.payments.api.PaymentInitializationRequest;
import com.vk42.cbp.firstmodule.payments.api.WebhookPayload;
import com.vk42.cbp.firstmodule.payments.domain.PaymentState;

public interface PaymentService {
    String initializePayment(PaymentInitializationRequest request);
    String processWebhooks(WebhookPayload payload);
    PaymentState currentPaymentState(Long paymentId);
}
