package com.ordershub.inventory.service;

import com.ordershub.inventory.domain.StockItem;
import com.ordershub.inventory.repository.StockItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    StockItemRepository stock;

    InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(stock);
    }

    @Test
    void shouldReserveAndDecrementWhenStockAvailable() {
        StockItem item = new StockItem(1L, 5);
        when(stock.findByProductId(1L)).thenReturn(Optional.of(item));

        boolean result = service.reserve(1L);

        assertThat(result).isTrue();
        assertThat(item.getQuantity()).isEqualTo(4);
        verify(stock).save(item);
    }

    @Test
    void shouldReturnFalseWhenProductNotFound() {
        when(stock.findByProductId(1L)).thenReturn(Optional.empty());

        boolean result = service.reserve(1L);

        assertThat(result).isFalse();
        verify(stock, never()).save(any());
    }

    @Test
    void shouldReturnFalseWhenStockIsEmpty() {
        StockItem item = new StockItem(1L, 0);
        when(stock.findByProductId(1L)).thenReturn(Optional.of(item));

        boolean result = service.reserve(1L);

        assertThat(result).isFalse();
        verify(stock, never()).save(any());
    }
}
