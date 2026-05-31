package com.nivesh.customer.service;

import com.nivesh.customer.dto.request.KycInitiationRequest;
import com.nivesh.library.dto.response.OtpResponse;
import org.springframework.web.multipart.MultipartFile;

public interface KycService {

    OtpResponse initiateKyc(KycInitiationRequest request, MultipartFile file);

    void verifyKyc(String requestId, String otp);
}
