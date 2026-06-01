package com.nivesh.account.service;

import com.nivesh.account.entity.Product;

public interface ProductService {

    /** Returns a product configuration by product code. */
    Product getProduct(String productCode);
}
