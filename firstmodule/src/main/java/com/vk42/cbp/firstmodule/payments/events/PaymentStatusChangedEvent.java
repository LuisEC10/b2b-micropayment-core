package com.vk42.cbp.firstmodule.payments.events;

import com.vk42.cbp.firstmodule.payments.domain.PaymentState;

public record PaymentStatusChangedEvent(
        Long paymentId,
        PaymentState previousState,
        PaymentState newState,
        String idempotencyKey,
        String json
) {
}
