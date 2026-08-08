package com.vk42.cbp.firstmodule.services;

import com.vk42.cbp.firstmodule.dto.PaymentInitializationRequest;
import com.vk42.cbp.firstmodule.dto.WebhookPayload;
import com.vk42.cbp.firstmodule.entities.PaymentState;

public interface PaymentService {
    String initializePayment(PaymentInitializationRequest request);
    String processWebhooks(WebhookPayload payload);
    PaymentState currentPaymentState(Long paymentId);
}
