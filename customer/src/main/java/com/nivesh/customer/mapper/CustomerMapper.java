package com.nivesh.customer.mapper;

import com.nivesh.customer.dto.request.CustomerRegisterRequest;
import com.nivesh.customer.dto.response.ContactResponse;
import com.nivesh.customer.dto.response.CustomerInfoResponse;
import com.nivesh.customer.dto.response.CustomerRegisterResponse;
import com.nivesh.customer.entity.Customer;
import com.nivesh.customer.entity.enums.Gender;
import com.nivesh.library.constant.Constants;
import com.nivesh.library.entity.enums.KycStatus;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mapper component for converting between customer domain objects and transfer objects.
 */
public class CustomerMapper {


    /**
     * Builds a Customer object from a request and provides necessary identifiers.
     *
     * @param request The CustomerRegisterRequest containing customer details.
     * @param userId The unique identifier for the customer.
     * @param customerNumber The customer's number.
     * @return A newly created Customer object.
     */
    public static Customer buildCustomer(CustomerRegisterRequest request, UUID userId, String customerNumber) {
        Gender gender = Gender.valueOf(request.getGender().toUpperCase());

        return Customer.builder()
                .userId(userId)
                .customerNumber(customerNumber)
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .gender(gender)
                .dateOfBirth(request.getDateOfBirth())
                .kycStatus(KycStatus.PENDING)
                .build();
    }


    /**
     * Creates a response map containing customer data and a token.
     *
     * @param customer The customer object.
     * @param token The authentication token.
     * @return A Map<String, Object> containing the customer's data and the token.
     */
    public static Map<String, Object> createResponse(Customer customer, String token) {
        CustomerInfoResponse customerResponse = buildCustomerResponse(customer);
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customerResponse);
        response.put("token", token);
        return response;
    }


    /**
     * Builds a full name string from a Customer object's first and last names.
     *
     * @param customer The Customer object containing the first and last names.
     * @return A string representing the full name, combining the first and last names.
     */
    public static String buildFullName(Customer customer) {
        return Stream.of(customer.getFirstName(), customer.getMiddleName(), customer.getLastName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }


    /**
     * Builds a CustomerInfoResponse object from a given Customer object.
     *
     * @param customer The Customer object to map.
     * @return A CustomerInfoResponse object containing the customer's information.
     */
    public static CustomerInfoResponse buildCustomerResponse(Customer customer) {
        Set<ContactResponse> contacts = customer.getContacts().stream()
                .map(contact -> new ContactResponse(
                        contact.getType(), contact.getEmail(), contact.getMobileNo()))
                .collect(Collectors.toSet());
        return CustomerInfoResponse.builder()
                .name(buildFullName(customer))
                .customerNumber(customer.getCustomerNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .kycStatus(customer.getKycStatus())
                .contacts(contacts)
                .build();

    }
}
