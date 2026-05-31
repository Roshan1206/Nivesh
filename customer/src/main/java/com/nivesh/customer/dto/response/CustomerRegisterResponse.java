package com.nivesh.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisterResponse {
    private String customerNumber;
    private String customerName;
    private String email;
    private String mobileNumber;
}
