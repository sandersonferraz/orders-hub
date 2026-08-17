package com.ordershub.catalog.service;

import com.ordershub.catalog.domain.Category;
import com.ordershub.catalog.repository.CategoryRepository;
import com.ordershub.catalog.exception.CategoryNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) { this.repository = repository; }

    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() { return repository.findAll(); }

    public Category create(Category category) { return repository.save(category); }

    @CacheEvict(value = "categories", key = "#id")
    public Category update(Long id, Category changes) {
        Category c = findById(id);
        c.setName(changes.getName());
        return repository.save(c);
    }

    @CacheEvict(value = "categories", key = "#id")
    public void delete(Long id) { repository.deleteById(id); }
}
