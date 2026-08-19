package com.ordershub.order.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, String customerId, BigDecimal total) {}