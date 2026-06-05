package com.nivesh.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object that carries address details dto details between layers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailsDto {

    /** Primary street address line. */
    private String streetLine1;

    /** Additional street address line. */
    private String streetLine2;

    /** City for the address. */
    private String city;

    /** State for the address. */
    private String State;

    /** Postal pin code for the address. */
    private String pinCode;

    /** Country for the address. */
    private String country;

    /** Type of address supplied by the client. */
    private String addressType;

    /** Verification status for the address. */
    private String addressStatus;
}
