package com.ordershub.order.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, Long productId, String customerId, BigDecimal total) {}