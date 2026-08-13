package com.vk42.cbp.firstmodule.payments.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentInitializationRequest(
        @NotNull(message = "the amount can't be null")
        @Positive(message = "the amount needs to be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "The currency needs to be provided")
        String currency,

        @NotBlank(message = "Idempotency Key is needed")
        String idempotencyKey
) {
}
