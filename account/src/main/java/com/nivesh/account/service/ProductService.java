package com.nivesh.account.service;

import com.nivesh.account.entity.Product;

/**
 * Service contract for account business logic related to product operations.
 */
public interface ProductService {

    /** Returns a product configuration by product code. */
    Product getProduct(String productCode);
}
