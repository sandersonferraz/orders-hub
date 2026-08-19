package com.ordershub.inventory.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.inventory.event.Events.PaymentApprovedEvent;
import com.ordershub.inventory.event.Events.StockOutEvent;
import com.ordershub.inventory.event.Events.StockReservedEvent;
import com.ordershub.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryConsumerTest {

    @Mock
    ObjectMapper mapper;

    @Mock
    InventoryService service;

    @Mock
    KafkaTemplate<String, String> kafka;

    InventoryConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryConsumer(mapper, service, kafka);
    }

    @Test
    void shouldPublishStockReservedEvent() throws Exception {
        String payload = "{\"orderId\":1,\"productId\":2,\"paymentId\":\"p-1\"}";
        PaymentApprovedEvent event = new PaymentApprovedEvent(1L, 2L, "p-1");
        when(mapper.readValue(payload, PaymentApprovedEvent.class)).thenReturn(event);
        when(service.reserve(2L)).thenReturn(true);
        when(mapper.writeValueAsString(any(StockReservedEvent.class))).thenReturn("{\"orderId\":1,\"productId\":2}");

        consumer.onPaymentApproved(payload);

        verify(kafka).send("inventory.events", "1", "{\"orderId\":1,\"productId\":2}");
    }

    @Test
    void shouldPublishStockOutEventWhenReserveFails() throws Exception {
        String payload = "{\"orderId\":1,\"productId\":2,\"paymentId\":\"p-1\"}";
        PaymentApprovedEvent event = new PaymentApprovedEvent(1L, 2L, "p-1");
        when(mapper.readValue(payload, PaymentApprovedEvent.class)).thenReturn(event);
        when(service.reserve(2L)).thenReturn(false);
        when(mapper.writeValueAsString(any(StockOutEvent.class))).thenReturn("{\"orderId\":1,\"productId\":2}");

        consumer.onPaymentApproved(payload);

        verify(kafka).send("inventory.events", "1", "{\"orderId\":1,\"productId\":2}");
    }

    @Test
    void shouldThrowWhenParsingFails() throws Exception {
        String payload = "{\"orderId\":1}";
        when(mapper.readValue(payload, PaymentApprovedEvent.class)).thenThrow(new RuntimeException("parse error"));

        assertThatThrownBy(() -> consumer.onPaymentApproved(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao dar baixa no estoque");
    }
}
