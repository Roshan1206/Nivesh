package com.nivesh.customer.mapper;

import com.nivesh.customer.entity.Contact;
import com.nivesh.customer.entity.Customer;
import com.nivesh.library.constant.Constants;

import java.util.Map;

/**
 * Mapper component for converting between customer domain objects and transfer objects.
 */
public class ContactMapper {

    public static Contact buildContact(Map<String, String> userInfo, Customer customer) {
        String email = userInfo.get(Constants.EMAIL);
        String mobileNo = userInfo.get(Constants.MOBILE);

        return Contact.builder()
                .email(email)
                .mobileNo(mobileNo)
                .type(Contact.ContactType.PRIMARY)
                .isVerified(true)
                .customer(customer)
                .build();
    }
}
