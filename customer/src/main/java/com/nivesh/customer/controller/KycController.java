package com.nivesh.customer.controller;

import com.nivesh.customer.dto.request.KycInitiationRequest;
import com.nivesh.customer.dto.request.KycVerificationRequest;
import com.nivesh.customer.service.KycService;
import com.nivesh.library.dto.response.OtpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller that exposes customer API endpoints for KYC operations.
 */
@RestController
@RequestMapping("/kyc")
public class KycController {

    /** Service that coordinates KYC initiation and verification. */
    private final KycService kycService;

    /**
     * Injects the KYC service used by KYC endpoints.
     */
    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping
    public ResponseEntity<OtpResponse> initiateKyc(@RequestPart("file") MultipartFile file,
                                                   @RequestPart("request") KycInitiationRequest request) {
        OtpResponse response = kycService.initiateKyc(request, file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * Verifies the KYC data based on the provided request ID and OTP.
     *
     * @param requestId The unique identifier for the KYC verification request.
     * @param otp The one-time password submitted by the user.
     * @return A ResponseEntity containing a success or error message.
     */
    @PostMapping(value = "/verify/{requestId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verifyKyc(@PathVariable String requestId, @RequestBody String otp) {
        kycService.verifyKyc(requestId, otp);
        return ResponseEntity.status(HttpStatus.OK).body("Kyc verified successfully.");
    }
}
