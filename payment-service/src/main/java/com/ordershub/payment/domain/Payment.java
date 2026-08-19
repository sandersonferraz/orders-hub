package com.ordershub.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private BigDecimal amount;

    protected Payment() {}

    public Payment(Long orderId, String status, BigDecimal amount) {
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
}