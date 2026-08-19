package com.ordershub.order.api;

import com.ordershub.order.domain.Order;
import com.ordershub.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    record CreateOrderRequest(@NotNull Long productId, @NotNull String customerId) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest body) {
        return service.create(body.productId(), body.customerId());
    }
}
