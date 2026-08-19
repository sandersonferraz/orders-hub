package com.ordershub.notification.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private Long orderId;
    private String type;     // ORDER_CREATED, PAYMENT_APPROVED, STOCK_RESERVED, ...
    private String payload;  // evento original (JSON)
    private Instant createdAt;

    protected Notification() {}

    public Notification(Long orderId, String type, String payload) {
        this.orderId = orderId;
        this.type = type;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public Long getOrderId() { return orderId; }
    public String getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}