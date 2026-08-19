package com.ordershub.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "catalog-service", path = "/products")
public interface CatalogClient {

    @GetMapping("/{id}")
    ProductResponse getProduct(@PathVariable("id") Long id);

    record ProductResponse(Long id, String name, BigDecimal price) {}
}
