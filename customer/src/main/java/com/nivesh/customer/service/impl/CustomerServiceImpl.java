package com.nivesh.customer.service.impl;

import com.nivesh.customer.dto.request.CustomerRegisterRequest;
import com.nivesh.customer.dto.response.CustomerInfoResponse;
import com.nivesh.customer.entity.Contact;
import com.nivesh.customer.entity.Customer;
import com.nivesh.customer.exception.CustomerNotFoundException;
import com.nivesh.customer.mapper.ContactMapper;
import com.nivesh.customer.mapper.CustomerMapper;
import com.nivesh.customer.repository.CustomerRepository;
import com.nivesh.customer.service.CustomerService;
import com.nivesh.customer.service.client.AuthServerClient;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.entity.enums.CustomerStatus;
import com.nivesh.library.entity.enums.KycStatus;
import com.nivesh.library.service.JwtTokenService;
import com.nivesh.library.service.SequenceGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Implementation of {@link CustomerService} for handling {@link Customer} related operations
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    /**
     * DB sequence name for generating next customer number
     */
    private final static String SEQUENCE = "seq_customer_number";

    /**
     * Synchronous call to auth server
     */
    private final AuthServerClient authServerClient;

    /**
     * DAO layer for performing DB operations in {@link Customer} entity
     */
    private final CustomerRepository customerRepository;

    /**
     * Performs token related operations
     */
    private final JwtTokenService jwtTokenService;

    /**
     * Next value generator
     */
    private final SequenceGenerator sequenceGenerator;

    /**
     * Injecting required dependency using CI.
     */
    public CustomerServiceImpl(AuthServerClient authServerClient, CustomerRepository customerRepository,
                               JwtTokenService jwtTokenService, SequenceGenerator sequenceGenerator) {
        this.authServerClient = authServerClient;
        this.customerRepository = customerRepository;
        this.jwtTokenService = jwtTokenService;
        this.sequenceGenerator = sequenceGenerator;
    }


    /**
     * Register a new Customer by getting basic details from {@link CustomerRegisterRequest}.
     *
     */
    @Transactional
    @Override
    public Map<String, Object> registerCustomer(CustomerRegisterRequest request) {
        Map<String, String> userInfo = jwtTokenService.getUserInfo();
        UUID userId = UUID.fromString(userInfo.get(Constants.USER_ID));

        String tokenType = jwtTokenService.getTokenType();
        if (!Constants.ONBOARDED_TOKEN.equals(tokenType) || customerRepository.existsByUserId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer has already been registered.");
        }
        String customerNumber = getNextCustomerNumber();
        Customer customer = CustomerMapper.buildCustomer(request, userId, customerNumber);
        Contact contact = ContactMapper.buildContact(userInfo, customer);

        customer.setContacts(Set.of(contact));
        customerRepository.save(customer);

        String token = authServerClient.updateUserStatus(userInfo.get(Constants.USER_ID),
                CustomerStatus.REGISTERED.name());

        return CustomerMapper.createResponse(customer, token);
    }


    @Transactional(readOnly = true)
    @Override
    public Customer getCustomer(String customerNumber) {
        return customerRepository.findByCustomerNumber(customerNumber).
                orElseThrow(CustomerNotFoundException::new);
    }

    @Override
    public CustomerInfoResponse getCustomerInfo(String customerNumber) {
        return CustomerMapper.buildCustomerResponse(getCustomer(customerNumber));
    }

    @Transactional
    @Override
    public void updateKycStaus(String customerNumber, KycStatus status) {
        Customer customer = getCustomer(customerNumber);
        customer.setKycStatus(status);
        customerRepository.save(customer);
    }

    private String getNextCustomerNumber() {
        long value = sequenceGenerator.generateNextSeqValue(SEQUENCE);
        return String.format("%08d", value);
    }
}
