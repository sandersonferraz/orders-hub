package com.ordershub.inventory.service;

import com.ordershub.inventory.domain.StockItem;
import com.ordershub.inventory.repository.StockItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final StockItemRepository stock;

    public InventoryService(StockItemRepository stock) {
        this.stock = stock;
    }

    @Transactional
    public boolean reserve(Long productId) {
        StockItem item = stock.findByProductId(productId).orElse(null);
        if (item == null || !item.reserve()) {
            return false;
        }
        stock.save(item);
        return true;
    }
}