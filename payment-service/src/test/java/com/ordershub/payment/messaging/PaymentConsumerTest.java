package com.ordershub.payment.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.payment.domain.Payment;
import com.ordershub.payment.event.Events.OrderCreatedEvent;
import com.ordershub.payment.event.Events.PaymentApprovedEvent;
import com.ordershub.payment.event.Events.PaymentRefusedEvent;
import com.ordershub.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTest {

    @Mock
    ObjectMapper mapper;

    @Mock
    PaymentService service;

    @Mock
    KafkaTemplate<String, String> kafka;

    PaymentConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentConsumer(mapper, service, kafka);
    }

    @Test
    void shouldPublishApprovedEvent() throws Exception {
        String payload = "{\"orderId\":1,\"productId\":1,\"customerId\":\"u-1\",\"total\":100.00}";
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, "u-1", new BigDecimal("100.00"));
        when(mapper.readValue(payload, OrderCreatedEvent.class)).thenReturn(event);

        Payment payment = new Payment(1L, "APPROVED", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(payment, "id", 42L);
        when(service.process(event.orderId(), event.total(), "4000000000000010")).thenReturn(payment);

        String approvedJson = "{\"orderId\":1,\"productId\":1,\"paymentId\":\"42\"}";
        when(mapper.writeValueAsString(any(PaymentApprovedEvent.class))).thenReturn(approvedJson);

        consumer.onOrderCreated(payload);

        verify(kafka).send("payments.events", "1", approvedJson);
    }

    @Test
    void shouldPublishRefusedEvent() throws Exception {
        String payload = "{\"orderId\":1,\"productId\":1,\"customerId\":\"u-1\",\"total\":100.00}";
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, "u-1", new BigDecimal("100.00"));
        when(mapper.readValue(payload, OrderCreatedEvent.class)).thenReturn(event);

        Payment payment = new Payment(1L, "REFUSED", new BigDecimal("100.00"));
        when(service.process(event.orderId(), event.total(), "4000000000000010")).thenReturn(payment);

        String refusedJson = "{\"orderId\":1,\"reason\":\"card refused\"}";
        when(mapper.writeValueAsString(any(PaymentRefusedEvent.class))).thenReturn(refusedJson);

        consumer.onOrderCreated(payload);

        verify(kafka).send("payments.events", "1", refusedJson);
    }

    @Test
    void shouldThrowWhenProcessingFails() throws Exception {
        String payload = "{\"orderId\":1}";
        when(mapper.readValue(payload, OrderCreatedEvent.class)).thenThrow(new RuntimeException("parse error"));

        assertThatThrownBy(() -> consumer.onOrderCreated(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao processar pedido");
    }
}
