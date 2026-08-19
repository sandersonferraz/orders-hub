package com.ordershub.payment.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.payment.event.Events.OrderCreatedEvent;
import com.ordershub.payment.event.Events.PaymentApprovedEvent;
import com.ordershub.payment.event.Events.PaymentRefusedEvent;
import com.ordershub.payment.service.PaymentService;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private final ObjectMapper mapper;
    private final PaymentService service;
    private final KafkaTemplate<String, String> kafka;

    public PaymentConsumer(ObjectMapper mapper, PaymentService service, KafkaTemplate<String, String> kafka) {
        this.mapper = mapper;
        this.service = service;
        this.kafka = kafka;
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 3000, multiplier = 2))
    @KafkaListener(topics = "orders.events", groupId = "payment-service")
    public void onOrderCreated(String payload) {
        try {
            OrderCreatedEvent event = mapper.readValue(payload, OrderCreatedEvent.class);
            var payment = service.process(event.orderId(), event.total(), "4000000000000010");
            String eventPayload = "APPROVED".equals(payment.getStatus())
                    ? mapper.writeValueAsString(new PaymentApprovedEvent(event.orderId(), payment.getId().toString()))
                    : mapper.writeValueAsString(new PaymentRefusedEvent(event.orderId(), "card refused"));
            // chave = orderId
            kafka.send("payments.events", String.valueOf(event.orderId()), eventPayload);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao processar pedido", e);
        }
    }

    @DltHandler
    public void onDlt(String payload) {
        // mensagens que esgotaram as tentativas caem na DLT (orders.events-dlt)
        // aqui você registra o erro para análise manual
        System.out.println("[DLT] falha definitiva: " + payload);
    }
}