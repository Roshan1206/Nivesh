package com.nivesh.account.repository;

import com.nivesh.account.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByProductCode(String productCode);

    @Query("SELECT MAX(a.productCode) FROM Product a")
    Optional<String> findMaxProductCode();
}