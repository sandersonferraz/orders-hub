package com.ordershub.catalog.service;

import com.ordershub.catalog.domain.Product;
import com.ordershub.catalog.domain.ProductDetails;
import com.ordershub.catalog.exception.ProductDetailsNotFoundException;
import com.ordershub.catalog.exception.ProductNotFoundException;
import com.ordershub.catalog.repository.ProductDetailsRepository;
import com.ordershub.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository repository;

    @Mock
    ProductDetailsRepository detailsRepository;

    ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository, detailsRepository);
    }

    private Product product(String name) {
        return new Product(name, "description", new BigDecimal("199.90"), 1L);
    }

    @Test
    void shouldReturnProduct() {
        Product product = product("Keyboard");
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        Product result = service.findById(1L);

        assertThat(result).isSameAs(product);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(product("A"), product("B")));

        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void shouldSaveAndReturnProduct() {
        Product product = product("New");
        when(repository.save(product)).thenReturn(product);

        Product result = service.create(product);

        assertThat(result).isSameAs(product);
        verify(repository).save(product);
    }

    @Test
    void shouldCopyFieldsAndSave() {
        Product existing = product("Old");
        Product changes = new Product("New", "new description", new BigDecimal("99.00"), null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Product result = service.update(1L, changes);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("new description");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("99.00"));
        verify(repository).save(existing);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentProduct() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, product("X")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldCallDeleteById() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void shouldSaveDetailsOfExistingProduct() {
        when(repository.findById(1L)).thenReturn(Optional.of(product("A")));
        ProductDetails details = new ProductDetails("1", "long description", new String[]{"gamer", "rgb"});
        when(detailsRepository.save(any(ProductDetails.class))).thenReturn(details);

        ProductDetails result = service.saveDetails(1L, "long description", new String[]{"gamer", "rgb"});

        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getTags()).containsExactly("gamer", "rgb");
        verify(detailsRepository).save(any(ProductDetails.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingDetailsOfNonexistentProduct() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveDetails(99L, "x", new String[]{"a"}))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldReturnDetails() {
        ProductDetails details = new ProductDetails("1", "long", new String[]{"a"});
        when(detailsRepository.findById("1")).thenReturn(Optional.of(details));

        assertThat(service.findDetails(1L)).isSameAs(details);
    }

    @Test
    void shouldThrowExceptionWhenDetailsNotFound() {
        when(detailsRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDetails(99L))
                .isInstanceOf(ProductDetailsNotFoundException.class);
    }
}
