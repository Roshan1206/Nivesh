package com.nivesh.customer.service;

import com.nivesh.customer.dto.AddressDetailsDto;

/**
 * Service contract for customer business logic related to address operations.
 */
public interface AddressService {

    AddressDetailsDto createNewAddress(AddressDetailsDto addressDetails);
}
