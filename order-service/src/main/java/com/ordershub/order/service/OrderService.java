package com.ordershub.order.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.order.client.CatalogClient;
import com.ordershub.order.domain.Order;
import com.ordershub.order.domain.OutboxEvent;
import com.ordershub.order.event.OrderCreatedEvent;
import com.ordershub.order.repository.OrderRepository;
import com.ordershub.order.repository.OutboxEventRepository;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OutboxEventRepository outbox;
    private final CatalogClient catalogClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakers;
    private final ObjectMapper mapper;

    public OrderService(OrderRepository orders, OutboxEventRepository outbox,
                        CatalogClient catalogClient,
                        CircuitBreakerFactory<?, ?> circuitBreakers,
                        ObjectMapper mapper) {
        this.orders = orders;
        this.outbox = outbox;
        this.catalogClient = catalogClient;
        this.circuitBreakers = circuitBreakers;
        this.mapper = mapper;
    }

    @Transactional
    public Order create(Long productId, String customerId) {
        // Consulta o catálogo com Circuit Breaker + fallback
        CatalogClient.ProductResponse product = circuitBreakers.create("catalog").run(
                () -> catalogClient.getProduct(productId),
                throwable -> new CatalogClient.ProductResponse(productId, "indisponível", BigDecimal.ZERO));

        Order order = orders.save(new Order(customerId, product.price(), "CREATED"));
        saveOutboxEvent(order, productId);
        return order;
    }

    private void saveOutboxEvent(Order order, Long productId) {
        try {
            OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), productId, order.getCustomerId(), order.getTotal());
            outbox.save(new OutboxEvent("Order", order.getId(), "OrderCreated", mapper.writeValueAsString(event)));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao serializar evento", e);
        }
    }
}
