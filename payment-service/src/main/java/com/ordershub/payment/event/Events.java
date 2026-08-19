package com.ordershub.payment.event;

import java.math.BigDecimal;

public final class Events {
    private Events() {}

    public record OrderCreatedEvent(Long orderId, Long productId, String customerId, BigDecimal total) {}
    public record PaymentApprovedEvent(Long orderId, Long productId, String paymentId) {}
    public record PaymentRefusedEvent(Long orderId, String reason) {}
}
