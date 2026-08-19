package com.ordershub.inventory.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.inventory.event.Events.PaymentApprovedEvent;
import com.ordershub.inventory.event.Events.StockOutEvent;
import com.ordershub.inventory.event.Events.StockReservedEvent;
import com.ordershub.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    private final ObjectMapper mapper;
    private final InventoryService service;
    private final KafkaTemplate<String, String> kafka;

    public InventoryConsumer(ObjectMapper mapper, InventoryService service, KafkaTemplate<String, String> kafka) {
        this.mapper = mapper;
        this.service = service;
        this.kafka = kafka;
    }

    @KafkaListener(topics = "payments.events", groupId = "inventory-service")
    public void onPaymentApproved(String payload) {
        try {
            PaymentApprovedEvent event = mapper.readValue(payload, PaymentApprovedEvent.class);
            Long productId = event.productId();
            String out = service.reserve(productId)
                    ? mapper.writeValueAsString(new StockReservedEvent(event.orderId(), productId))
                    : mapper.writeValueAsString(new StockOutEvent(event.orderId(), productId));
            kafka.send("inventory.events", String.valueOf(event.orderId()), out);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao dar baixa no estoque", e);
        }
    }
}