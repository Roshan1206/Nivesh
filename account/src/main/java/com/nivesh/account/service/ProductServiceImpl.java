package com.nivesh.account.service;

import com.nivesh.account.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository){
        this.repository = repository;
    }

    private String generateId(){
        String lastCode = repository.findMaxProductCode().orElseThrow();
        int next = Integer.parseInt(lastCode) + 1;
        return String.format("%03d", next);
    }
}
