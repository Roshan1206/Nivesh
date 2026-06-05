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

    public static Map<String, Object> createResponse(Customer customer, String token) {
        CustomerInfoResponse customerResponse = buildCustomerResponse(customer);
        Map<String, Object> response = new HashMap<>();
        response.put("customer", customerResponse);
        response.put("token", token);
        return response;
    }

    public static String buildFullName(Customer customer) {
        return Stream.of(customer.getFirstName(), customer.getMiddleName(), customer.getLastName())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

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
