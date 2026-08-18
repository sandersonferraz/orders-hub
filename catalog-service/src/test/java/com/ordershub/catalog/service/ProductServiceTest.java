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
        return new Product(name, "descrição", new BigDecimal("199.90"), 1L);
    }

    @Test
    void findById_deveRetornarProduto() {
        Product product = product("Teclado");
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        Product result = service.findById(1L);

        assertThat(result).isSameAs(product);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findAll_deveRetornarLista() {
        when(repository.findAll()).thenReturn(List.of(product("A"), product("B")));

        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void create_deveSalvarEDevolverProduto() {
        Product product = product("Novo");
        when(repository.save(product)).thenReturn(product);

        Product result = service.create(product);

        assertThat(result).isSameAs(product);
        verify(repository).save(product);
    }

    @Test
    void update_deveCopiarCamposESalvar() {
        Product existing = product("Antigo");
        Product changes = new Product("Novo", "nova descrição", new BigDecimal("99.00"), null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Product result = service.update(1L, changes);

        assertThat(result.getName()).isEqualTo("Novo");
        assertThat(result.getDescription()).isEqualTo("nova descrição");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("99.00"));
        verify(repository).save(existing);
    }

    @Test
    void update_deveLancarExcecaoQuandoProdutoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, product("X")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_deveChamarDeleteById() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void saveDetails_deveSalvarDetalhesDoProdutoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(product("A")));
        ProductDetails details = new ProductDetails("1", "longa descrição", new String[]{"gamer", "rgb"});
        when(detailsRepository.save(any(ProductDetails.class))).thenReturn(details);

        ProductDetails result = service.saveDetails(1L, "longa descrição", new String[]{"gamer", "rgb"});

        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getTags()).containsExactly("gamer", "rgb");
        verify(detailsRepository).save(any(ProductDetails.class));
    }

    @Test
    void saveDetails_deveLancarExcecaoQuandoProdutoNaoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveDetails(99L, "x", new String[]{"a"}))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findDetails_deveRetornarDetalhes() {
        ProductDetails details = new ProductDetails("1", "longa", new String[]{"a"});
        when(detailsRepository.findById("1")).thenReturn(Optional.of(details));

        assertThat(service.findDetails(1L)).isSameAs(details);
    }

    @Test
    void findDetails_deveLancarExcecaoQuandoNaoExiste() {
        when(detailsRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findDetails(99L))
                .isInstanceOf(ProductDetailsNotFoundException.class);
    }
}
