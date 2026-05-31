package com.nivesh.customer.service;

import com.nivesh.customer.dto.AddressDetailsDto;

public interface AddressService {

    AddressDetailsDto createNewAddress(AddressDetailsDto addressDetails);
}
