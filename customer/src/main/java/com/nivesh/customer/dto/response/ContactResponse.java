package com.nivesh.customer.dto.response;

import com.nivesh.customer.entity.Contact;
import lombok.*;

/**
 * Response payload returned by the customer API for contact response operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    /** Type of contact returned to the client. */
    private Contact.ContactType contactType;

    /** Email address supplied by the client. */
    private String email;

    /** Mobile number returned to or supplied by the client. */
    private String mobileNumber;
}
