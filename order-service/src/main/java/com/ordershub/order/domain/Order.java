package com.ordershub.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    private BigDecimal total;

    private String status; // CREATED, PAYMENT_APPROVED, PAYMENT_REFUSED, ...

    @Version
    private Long version;

    protected Order() {}

    public Order(String customerId, BigDecimal total, String status) {
        this.customerId = customerId;
        this.total = total;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}