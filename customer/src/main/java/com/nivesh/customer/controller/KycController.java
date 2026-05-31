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

@RestController
@RequestMapping("/{customerNumber}/kyc")
public class KycController {

    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping
    public ResponseEntity<OtpResponse> initiateKyc(@RequestPart("file") MultipartFile file,
                                                   @RequestPart("request") KycInitiationRequest request) {
        OtpResponse response = kycService.initiateKyc(request, file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/verify/{requestId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verifyKyc(@PathVariable String requestId, @RequestBody String otp) {
        kycService.verifyKyc(requestId, otp);
        return ResponseEntity.status(HttpStatus.OK).body("Kyc verified successfully.");
    }
}
