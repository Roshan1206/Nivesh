package com.nivesh.customer.dto.response;

import com.nivesh.customer.entity.enums.Gender;
import com.nivesh.library.entity.enums.KycStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

/**
 * Response payload returned by the customer API for customer info response operations.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoResponse {

    /** Name value used by this component. */
    private String name;

    /** Customer number value used by this component. */
    private String customerNumber;

    /** Customer date of birth. */
    private LocalDate dateOfBirth;

    /** Customer gender value. */
    private Gender gender;

    /** Kyc status value used by this component. */
    private KycStatus kycStatus;

    /** Contacts returned for the customer. */
    private Set<ContactResponse> contacts;
}
