package com.ordershub.inventory.event;

public final class Events {
    private Events() {}

    public record PaymentApprovedEvent(Long orderId, Long productId, String paymentId) {}
    public record StockReservedEvent(Long orderId, Long productId) {}
    public record StockOutEvent(Long orderId, Long productId) {}
}