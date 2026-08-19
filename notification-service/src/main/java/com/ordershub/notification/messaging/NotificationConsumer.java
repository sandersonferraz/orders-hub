package com.ordershub.notification.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.notification.domain.Notification;
import com.ordershub.notification.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final ObjectMapper mapper;
    private final NotificationRepository notifications;

    public NotificationConsumer(ObjectMapper mapper, NotificationRepository notifications) {
        this.mapper = mapper;
        this.notifications = notifications;
    }

    @KafkaListener(topics = "orders.events", groupId = "notification-service")
    public void onOrder(String payload) {
        save(payload, "ORDER_CREATED");
    }

    @KafkaListener(topics = "payments.events", groupId = "notification-service")
    public void onPayment(String payload) {
        save(payload, "PAYMENT_EVENT");
    }

    @KafkaListener(topics = "inventory.events", groupId = "notification-service")
    public void onInventory(String payload) {
        save(payload, "INVENTORY_EVENT");
    }

    private void save(String payload, String type) {
        try {
            JsonNode node = mapper.readTree(payload);
            Long orderId = node.hasNonNull("orderId") ? node.get("orderId").asLong() : null;
            notifications.save(new Notification(orderId, type, payload));
        } catch (Exception e) {
            throw new RuntimeException("Falha ao persistir notificação", e);
        }
    }
}