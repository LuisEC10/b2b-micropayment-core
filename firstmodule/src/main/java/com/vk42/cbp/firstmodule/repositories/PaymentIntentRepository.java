package com.vk42.cbp.firstmodule.repositories;

import com.vk42.cbp.firstmodule.entities.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

}
