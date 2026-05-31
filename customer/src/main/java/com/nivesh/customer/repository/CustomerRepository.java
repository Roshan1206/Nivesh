package com.nivesh.customer.repository;

import com.nivesh.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByUserId(UUID id);

    Optional<Customer> findByCustomerNumber(String customerNumber);
}
