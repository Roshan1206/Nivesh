package com.nivesh.customer.service;

import com.nivesh.customer.dto.request.CustomerRegisterRequest;
import com.nivesh.customer.dto.response.CustomerInfoResponse;
import com.nivesh.customer.entity.Customer;
import com.nivesh.library.entity.enums.KycStatus;

import java.util.Map;

/**
 * Define operations for managing customers
 */
public interface CustomerService {

    /**
     * Register a new customer
     */
    Map<String, Object> registerCustomer(CustomerRegisterRequest request);

    /**
     * Get full customer data
     */
    Customer getCustomer(String customerNumber);

    CustomerInfoResponse getCustomerInfo();
    /**
     * Update customer kyc status
     */
    void updateKycStaus(String customerNumber, KycStatus status);
}