package com.nivesh.customer.dto.response;

import com.nivesh.customer.entity.Contact;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private Contact.ContactType contactType;
    private String email;
    private String mobileNumber;
}
