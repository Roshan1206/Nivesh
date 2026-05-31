package com.nivesh.customer.repository;

import com.nivesh.customer.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Query("""
            SELECT c.email FROM Contact c
            WHERE c.customer.id = :customer AND c.type = :contactType
            """)
    String getCustomerPrimaryEmail(@Param("customer") UUID customer, @Param("contactType") Contact.ContactType contactType);
}
