package com.ordershub.payment.event;

import java.math.BigDecimal;

public final class Events {
    private Events() {}

    public record OrderCreatedEvent(Long orderId, String customerId, BigDecimal total) {}
    public record PaymentApprovedEvent(Long orderId, String paymentId) {}
    public record PaymentRefusedEvent(Long orderId, String reason) {}
}
