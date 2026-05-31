package com.nivesh.customer.dto.response;

import com.nivesh.customer.entity.enums.Gender;
import com.nivesh.library.entity.enums.KycStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoResponse {

    private String name;
    private String customerNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private KycStatus kycStatus;
    private Set<ContactResponse> contacts;
}
