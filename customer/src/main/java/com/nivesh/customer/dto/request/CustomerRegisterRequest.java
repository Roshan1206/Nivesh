package com.nivesh.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request payload used by the customer API for customer register request operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterRequest {

    /** Customer first name. */
    private String firstName;

    /** Customer middle name, when provided. */
    private String middleName;

    /** Customer last name. */
    private String lastName;

    /** Customer date of birth. */
    private LocalDate dateOfBirth;

    /** Customer gender value. */
    private String gender;
}
