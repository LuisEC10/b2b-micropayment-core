package com.vk42.cbp.firstmodule.payments;

import com.vk42.cbp.firstmodule.payments.api.PaymentController;
import com.vk42.cbp.firstmodule.payments.domain.PaymentState;
import com.vk42.cbp.firstmodule.payments.service.PaymentService;
import com.vk42.cbp.firstmodule.security.jws.WorkerSignatureService;
import com.vk42.cbp.firstmodule.shared.exceptions.GlobalExceptionHandler;
import com.vk42.cbp.firstmodule.shared.exceptions.IllegalStateTransactionException;
import com.vk42.cbp.firstmodule.shared.exceptions.PaymentNotFoundException;
import com.vk42.cbp.firstmodule.shared.filters.RequestIdFilter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({RequestIdFilter.class, GlobalExceptionHandler.class})
public class PaymentCoreStateAndIdempotencyTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private WorkerSignatureService workerSignatureService;

    @Test
    @DisplayName("MUST VALIDATE THE STATE OF TRANSACTIONS")
    public void canTransitionToTest() {
        assertTrue(PaymentState.CREATED.canTransitionTo(PaymentState.AUTHORIZED));
        assertFalse(PaymentState.CREATED.canTransitionTo(PaymentState.CONFIRMED));
    }

    @Test
    @DisplayName("Must Inject X-Request-ID in HTTP Response")
    public void requestIdFilter() throws Exception{
        this.mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    @DisplayName("Must return PAYMENT_NOT_FOUND (404) when payment does not exist")
    public void paymentNotFoundExceptionTest() throws Exception {
        given(this.paymentService.currentPaymentState(999L))
                .willThrow(new PaymentNotFoundException("Payment Intent Not Found"));

        this.mockMvc.perform(get("/api/v1/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Payment Intent Not Found"));
    }

    @Test
    @DisplayName("Must Return INVALID_STATE_TRANSACTION (409) when Illegal Transaction")
    public void illegalStateTransactionExceptionTest() throws Exception {
        given(paymentService.processWebhooks(any()))
                .willThrow(new IllegalStateTransactionException("Transaction not permitted from CREATED to CONFIRMED"));
        String payloadJson = """
                {
                    "paymentId": 1,
                    "newState": "CONFIRMED",
                    "idempotencyKey": "KEY_123"
                }
                """;
        mockMvc.perform(post("/api/v1/payments/webhooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATE_TRANSITION"));
    }
}
