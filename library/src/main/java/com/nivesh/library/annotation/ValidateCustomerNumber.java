package com.nivesh.library.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^\\d{8}$", message = "Customer number must be of 8 digits")
public @interface ValidateCustomerNumber {
    String message() default "Customer number must be of 8 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}