package com.nivesh.customer.service.impl;

import com.nivesh.customer.entity.Contact;
import com.nivesh.customer.repository.ContactRepository;
import com.nivesh.customer.service.ContactService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;

    public ContactServiceImpl(ContactRepository repository){
        this.repository = repository;
    }

    @Override
    public String getCustomerEmail(UUID customerId) {
        return repository.getCustomerPrimaryEmail(customerId, Contact.ContactType.PRIMARY);
    }
}
