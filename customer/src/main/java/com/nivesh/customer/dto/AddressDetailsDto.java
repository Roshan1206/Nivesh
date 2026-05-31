package com.nivesh.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetailsDto {

    private String streetLine1;
    private String streetLine2;
    private String city;
    private String State;
    private String pinCode;
    private String country;
    private String addressType;
    private String addressStatus;
}
