package com.ordershub.order.api;

import com.ordershub.order.domain.Order;
import com.ordershub.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    OrderService service;

    OrderController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderController(service);
    }

    @Test
    void shouldCreateOrder() {
        Order order = new Order("customer-1", new BigDecimal("199.90"), "CREATED");
        when(service.create(10L, "customer-1")).thenReturn(order);

        Order result = controller.create(new OrderController.CreateOrderRequest(10L, "customer-1"));

        assertThat(result).isSameAs(order);
        verify(service).create(10L, "customer-1");
    }
}
