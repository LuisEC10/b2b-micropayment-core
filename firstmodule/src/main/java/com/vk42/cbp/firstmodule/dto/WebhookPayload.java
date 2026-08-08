package com.vk42.cbp.firstmodule.dto;

import com.vk42.cbp.firstmodule.entities.PaymentState;

public record WebhookPayload(
        Long paymentId,
        PaymentState newState,
        String idempotencyKey
) {
}
