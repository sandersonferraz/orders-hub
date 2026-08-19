package com.ordershub.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordershub.order.client.CatalogClient;
import com.ordershub.order.domain.Order;
import com.ordershub.order.domain.OutboxEvent;
import com.ordershub.order.repository.OrderRepository;
import com.ordershub.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orders;

    @Mock
    OutboxEventRepository outbox;

    @Mock
    CatalogClient catalogClient;

    @Mock
    CircuitBreakerFactory<?, ?> circuitBreakers;

    @Mock
    CircuitBreaker circuitBreaker;

    OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(orders, outbox, catalogClient, circuitBreakers, new ObjectMapper());
    }

    @Test
    void shouldCreateOrderAndSaveOutboxEvent() {
        Long productId = 10L;
        String customerId = "customer-1";
        CatalogClient.ProductResponse product =
                new CatalogClient.ProductResponse(productId, "Keyboard", new BigDecimal("199.90"));

        when(circuitBreakers.create("catalog")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any()))
                .thenAnswer(inv -> ((Supplier<CatalogClient.ProductResponse>) inv.getArgument(0)).get());
        when(catalogClient.getProduct(productId)).thenReturn(product);
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = service.create(productId, customerId);

        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getTotal()).isEqualByComparingTo("199.90");
        assertThat(result.getStatus()).isEqualTo("CREATED");
        verify(orders).save(any(Order.class));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outbox).save(captor.capture());
        OutboxEvent event = captor.getValue();
        assertThat(event.getPayload()).contains(customerId);
    }

    @Test
    void shouldUseFallbackWhenCatalogFails() {
        Long productId = 10L;
        String customerId = "customer-1";

        when(circuitBreakers.create("catalog")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any()))
                .thenAnswer(inv -> ((Function<Throwable, CatalogClient.ProductResponse>) inv.getArgument(1))
                        .apply(new RuntimeException("catalog down")));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = service.create(productId, customerId);

        assertThat(result.getTotal()).isEqualByComparingTo("0");
    }
}
