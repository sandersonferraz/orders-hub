package com.ordershub.catalog.exception;

public class ProductDetailsNotFoundException extends RuntimeException {
    public ProductDetailsNotFoundException(Long id) {
        super("Detalhes não encontrados para o produto: id " + id);
    }
}