package com.nivesh.customer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request payload used by the customer API for KYC initiation request operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycInitiationRequest {

    /** Name value used by this component. */
    private String name;

    /** Customer number value used by this component. */
    private String customerNumber;

    /** Customer date of birth. */
    private LocalDate dateOfBirth;

    /** Identifier printed on the submitted document. */
    private String documentNumber;

    /** Type of KYC document submitted. */
    private String documentType;
}
