package com.ordershub.catalog.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Size(max = 2000)
    private String description;

    @Column(nullable = false)
    @Positive
    private BigDecimal price;

    private Long categoryId;

    @Version
    private Long version;

    protected Product() {}

    public Product(String name, String description, BigDecimal price, Long categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
    }

    // getters e setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Long getCategoryId() { return categoryId; }
    public Long getVersion() { return version; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}