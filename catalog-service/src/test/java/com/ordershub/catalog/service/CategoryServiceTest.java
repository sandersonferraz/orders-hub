package com.ordershub.catalog.service;

import com.ordershub.catalog.domain.Category;
import com.ordershub.catalog.exception.CategoryNotFoundException;
import com.ordershub.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository repository;

    CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(repository);
    }

    @Test
    void shouldReturnCategory() {
        Category category = new Category("Peripherals");
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(service.findById(1L)).isSameAs(category);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void shouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(new Category("A"), new Category("B")));

        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    void shouldSaveAndReturnCategory() {
        Category category = new Category("New");
        when(repository.save(category)).thenReturn(category);

        assertThat(service.create(category)).isSameAs(category);
        verify(repository).save(category);
    }

    @Test
    void shouldCopyNameAndSave() {
        Category existing = new Category("Old");
        Category changes = new Category("New");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Category result = service.update(1L, changes);

        assertThat(result.getName()).isEqualTo("New");
        verify(repository).save(existing);
    }

    @Test
    void shouldCallDeleteById() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }
}
