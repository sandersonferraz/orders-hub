package com.ordershub.order.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType; // "Order"
    private Long aggregateId;     // orderId
    private String eventType;     // "OrderCreated"
    @Column(columnDefinition = "TEXT")
    private String payload;       // JSON
    private boolean published;

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public String getPayload() { return payload; }
    public Long getAggregateId() { return aggregateId; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}