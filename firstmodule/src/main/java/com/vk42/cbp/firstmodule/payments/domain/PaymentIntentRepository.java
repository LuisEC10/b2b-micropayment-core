package com.vk42.cbp.firstmodule.payments.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

}
