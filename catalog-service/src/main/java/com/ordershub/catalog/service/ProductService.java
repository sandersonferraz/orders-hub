package com.ordershub.catalog.service;

import com.ordershub.catalog.domain.Product;
import com.ordershub.catalog.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ordershub.catalog.exception.ProductNotFoundException;

import java.util.List;
import com.ordershub.catalog.domain.ProductDetails;
import com.ordershub.catalog.repository.ProductDetailsRepository;
import com.ordershub.catalog.exception.ProductDetailsNotFoundException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final ProductDetailsRepository detailsRepository;


    public ProductService(ProductRepository repository, ProductDetailsRepository detailsRepository) {
        this.repository = repository;
        this.detailsRepository = detailsRepository;
    }
    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product create(Product product) {
        return repository.save(product);
    }

    @CacheEvict(value = "products", key = "#id")
    public Product update(Long id, Product changes) {
        Product p = findById(id);
        p.setName(changes.getName());
        p.setDescription(changes.getDescription());
        p.setPrice(changes.getPrice());
        return repository.save(p);
    }

    @CacheEvict(value = "products", key = "#id")
    public void delete(Long id) {
        repository.deleteById(id);
    }

    public ProductDetails saveDetails(Long productId, String longDescription, String[] tags) {
        findById(productId); // garante que o produto existe no Postgres
        return detailsRepository.save(new ProductDetails(productId.toString(), longDescription, tags));
    }

    public ProductDetails findDetails(Long productId) {
        return detailsRepository.findById(productId.toString())
                .orElseThrow(() -> new ProductDetailsNotFoundException(productId));
    }
}