package com.nivesh.customer.service.impl;

import com.nivesh.customer.entity.Contact;
import com.nivesh.customer.repository.ContactRepository;
import com.nivesh.customer.service.ContactService;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation that coordinates customer business logic for contact operations.
 */
@Service
public class ContactServiceImpl implements ContactService {

    /** Repository used to manage customer contact records. */
    private final ContactRepository repository;

    /**
     * Injects the contact repository used to manage customer contacts.
     */
    public ContactServiceImpl(ContactRepository repository){
        this.repository = repository;
    }

    @Override
    public String getCustomerEmail(UUID customerId) {
        return repository.getCustomerPrimaryEmail(customerId, Contact.ContactType.PRIMARY);
    }
}
