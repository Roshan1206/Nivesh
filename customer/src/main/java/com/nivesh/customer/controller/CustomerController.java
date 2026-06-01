package com.nivesh.customer.controller;

import com.nivesh.customer.dto.request.CustomerRegisterRequest;
import com.nivesh.customer.dto.response.CustomerInfoResponse;
import com.nivesh.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        Map<String, Object> response = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/info")
    public ResponseEntity<CustomerInfoResponse> getCustomerInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getCustomerInfo());
    }
}