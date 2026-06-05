package com.nivesh.customer.service;

import java.util.UUID;

/**
 * Service contract for customer business logic related to contact operations.
 */
public interface ContactService {

    String getCustomerEmail(UUID customerId);

}
