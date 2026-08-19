package com.ordershub.inventory.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Version
    private Long version;

    protected StockItem() {}

    public StockItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public int getQuantity() { return quantity; }

    public boolean reserve() {
        if (quantity <= 0) {
            return false;
        }
        quantity--;
        return true;
    }
}