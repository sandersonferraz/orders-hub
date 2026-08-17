package com.ordershub.catalog.api;

import com.ordershub.catalog.domain.Product;
import com.ordershub.catalog.domain.ProductDetails;
import com.ordershub.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public record DetailsRequest(String longDescription, String[] tags) {}

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody Product product) {
        return service.create(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product product) {
        return service.update(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/details")
    public ProductDetails getDetails(@PathVariable Long id) {
        return service.findDetails(id);
    }

    @PutMapping("/{id}/details")
    public ProductDetails saveDetails(@PathVariable Long id, @RequestBody DetailsRequest request) {
        return service.saveDetails(id, request.longDescription(), request.tags());
    }
}