package com.vk42.cbp.firstmodule.payments.api;

import com.vk42.cbp.firstmodule.payments.domain.PaymentState;

public record WebhookPayload(
        Long paymentId,
        PaymentState newState,
        String idempotencyKey
) {
}
