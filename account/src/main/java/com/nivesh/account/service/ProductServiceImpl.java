package com.nivesh.account.service;

import com.nivesh.account.entity.Product;
import com.nivesh.account.exception.ProductNotFoundException;
import com.nivesh.account.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository){
        this.productRepository = productRepository;
    }

    @Override
    public Product getProduct(String productCode) {
        return productRepository.findByProductCode(productCode).orElseThrow(
                () -> new ProductNotFoundException(HttpStatus.NOT_FOUND, "Product not found with given code")
        );
    }

    private String generateId(){
        String lastCode = productRepository.findMaxProductCode().orElseThrow();
        int next = Integer.parseInt(lastCode) + 1;
        return String.format("%03d", next);
    }
}