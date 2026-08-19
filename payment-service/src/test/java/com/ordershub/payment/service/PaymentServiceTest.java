package com.ordershub.payment.service;

import com.ordershub.payment.client.PagarMeClient;
import com.ordershub.payment.domain.Payment;
import com.ordershub.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository payments;

    @Mock
    PagarMeClient pagarMe;

    PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, pagarMe);
    }

    @Test
    void shouldReturnExistingPaymentWithoutChargingAgain() {
        Long orderId = 1L;
        Payment existing = new Payment(orderId, "APPROVED", new BigDecimal("100.00"));
        when(payments.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        Payment result = service.process(orderId, new BigDecimal("100.00"), "4000000000000010");

        assertThat(result).isSameAs(existing);
        verify(pagarMe, never()).createOrder(any(), any(), any());
        verify(payments, never()).save(any());
    }

    @Test
    void shouldApprovePaymentWhenGatewayReturnsPaid() {
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        when(payments.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(pagarMe.createOrder(orderId, amount, "4000000000000010"))
                .thenReturn(new PagarMeClient.PaymentResult("paid", "pay_123"));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = service.process(orderId, amount, "4000000000000010");

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getAmount()).isEqualByComparingTo(amount);
        verify(payments).save(any(Payment.class));
    }

    @Test
    void shouldRefusePaymentWhenGatewayDoesNotReturnPaid() {
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        when(payments.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(pagarMe.createOrder(orderId, amount, "4000000000000010"))
                .thenReturn(new PagarMeClient.PaymentResult("refused", "pay_123"));
        when(payments.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = service.process(orderId, amount, "4000000000000010");

        assertThat(result.getStatus()).isEqualTo("REFUSED");
        verify(payments).save(any(Payment.class));
    }
}
