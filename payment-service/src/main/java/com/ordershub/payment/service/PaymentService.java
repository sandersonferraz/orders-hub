package com.ordershub.payment.service;

import com.ordershub.payment.client.PagarMeClient;
import com.ordershub.payment.domain.Payment;
import com.ordershub.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final PagarMeClient pagarMe;

    public PaymentService(PaymentRepository payments, PagarMeClient pagarMe) {
        this.payments = payments;
        this.pagarMe = pagarMe;
    }

    @Transactional
    public Payment process(Long orderId, BigDecimal amount, String cardNumber) {
        // idempotência: se já existe pagamento para o pedido, devolve sem cobrar de novo
        return payments.findByOrderId(orderId)
                .orElseGet(() -> {
                    PagarMeClient.PaymentResult result = pagarMe.createOrder(orderId, amount, cardNumber);
                    String status = "paid".equalsIgnoreCase(result.status()) ? "APPROVED" : "REFUSED";
                    return payments.save(new Payment(orderId, status, amount));
                });
    }
}