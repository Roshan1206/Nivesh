package com.nivesh.customer.repository;

import com.nivesh.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /** Checks whether a user already has a customer record. */
    boolean existsByUserId(UUID id);

    /** Finds a customer record by the authenticated user identifier. */
    Optional<Customer> findByUserId(UUID userId);

    /** Finds a customer record by the generated customer number. */
    Optional<Customer> findByCustomerNumber(String customerNumber);
}
