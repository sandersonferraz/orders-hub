package com.ordershub.order.messaging;

import com.ordershub.order.domain.OutboxEvent;
import com.ordershub.order.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) {
        this.outbox = outbox;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outbox.findByPublishedFalse();
        for (OutboxEvent e : pending) {
            try {
                // chave de partição = orderId → garante ordem por pedido
                kafka.send("orders.events", String.valueOf(e.getAggregateId()), e.getPayload()).get();
                e.setPublished(true); // só marca publicado se o envio confirmar
            } catch (Exception ex) {
                // mantém published=false e tenta de novo no próximo ciclo
            }
        }
    }
}