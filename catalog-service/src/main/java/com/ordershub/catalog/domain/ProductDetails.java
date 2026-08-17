package com.ordershub.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "product_details")
public class ProductDetails {

    @Id
    private String id;       // id do produto no Postgres (espelhado)
    private String longDescription;
    private String[] tags;

    public ProductDetails(String id, String longDescription, String[] tags) {
        this.id = id;
        this.longDescription = longDescription;
        this.tags = tags;
    }

    public String getId() { return id; }
    public String getLongDescription() { return longDescription; }
    public String[] getTags() { return tags; }
}