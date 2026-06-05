package com.nivesh.customer.repository;

import com.nivesh.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for persisting and querying address records.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
