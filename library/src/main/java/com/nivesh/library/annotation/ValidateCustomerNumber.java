package com.nivesh.library.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reusable validation annotation for eight-digit customer numbers shared across services.
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^\\d{8}$", message = "Customer number must be of 8 digits")
public @interface ValidateCustomerNumber {
    /** Validation message returned when the customer number does not match the expected format. */
    String message() default "Customer number must be of 8 digits";
    /** Bean Validation groups for consumers that need grouped validation. */
    Class<?>[] groups() default {};
    /** Optional payload metadata for Bean Validation clients. */
    Class<? extends Payload>[] payload() default {};
}
