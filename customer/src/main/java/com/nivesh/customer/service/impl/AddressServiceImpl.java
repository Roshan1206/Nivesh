package com.nivesh.customer.service.impl;

import com.nivesh.customer.dto.AddressDetailsDto;
import com.nivesh.customer.service.AddressService;
import org.springframework.stereotype.Service;

/**
 * Service implementation that coordinates customer business logic for address operations.
 */
@Service
public class AddressServiceImpl implements AddressService {


    /**
     * Creates a new AddressDetailsDto object and returns it.
     *
     * @param addressDetails The AddressDetailsDto object to create.
     * @return The newly created AddressDetailsDto object.
     */
    @Override
    public AddressDetailsDto createNewAddress(AddressDetailsDto addressDetails) {
        return null;
    }
}
