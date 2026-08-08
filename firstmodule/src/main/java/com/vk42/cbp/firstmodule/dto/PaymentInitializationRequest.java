package com.vk42.cbp.firstmodule.dto;

import java.math.BigDecimal;

public record PaymentInitializationRequest(
        BigDecimal amount,
        String currency,
        String idempotencyKey
) {
}
