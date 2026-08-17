package com.ordershub.catalog.repository;

import com.ordershub.catalog.domain.ProductDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductDetailsRepository extends MongoRepository<ProductDetails, String> {
}