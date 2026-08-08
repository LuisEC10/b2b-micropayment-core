package com.vk42.cbp.firstmodule.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Por defecto, optimistic locking
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;

    @Column(unique = true)
    private String idempotencyKey;

    private BigDecimal amount;

    private String currency;
}
