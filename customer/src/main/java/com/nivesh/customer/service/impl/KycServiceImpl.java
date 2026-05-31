package com.nivesh.customer.service.impl;

import com.nivesh.customer.dto.request.KycInitiationRequest;
import com.nivesh.customer.entity.Customer;
import com.nivesh.customer.entity.KycDocument;
import com.nivesh.customer.repository.KycDocumentRepository;
import com.nivesh.customer.service.ContactService;
import com.nivesh.customer.service.CustomerService;
import com.nivesh.customer.service.KycService;
import com.nivesh.customer.service.client.AuthServerClient;
import com.nivesh.library.cache.OtpCacheService;
import com.nivesh.library.dto.response.OtpResponse;
import com.nivesh.library.dto.response.OtpStore;
import com.nivesh.library.entity.enums.CustomerStatus;
import com.nivesh.library.entity.enums.KycStatus;
import com.nivesh.library.entity.enums.OtpPurpose;
import com.nivesh.library.service.JwtTokenService;
import com.nivesh.library.cache.OtpSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;


@Service
public class KycServiceImpl implements KycService {

    private final AuthServerClient authServerClient;

    private final ContactService contactService;

    private final CustomerService customerService;

    private final OtpCacheService otpCacheService;

    private final KycDocumentRepository repository;

    private final JwtTokenService jwtTokenService;

    private final OtpSender otpSender;

    public KycServiceImpl(AuthServerClient authServerClient, ContactService contactService,
                          CustomerService customerService, KycDocumentRepository repository,
                          JwtTokenService jwtTokenService, OtpCacheService otpCacheService,
                          @Qualifier("emailOtpSender") OtpSender otpSender) {
        this.authServerClient = authServerClient;
        this.contactService = contactService;
        this.customerService = customerService;
        this.repository = repository;
        this.jwtTokenService = jwtTokenService;
        this.otpCacheService = otpCacheService;
        this.otpSender = otpSender;
    }

//    TODO: create validation method for kyc using UIDAI/NSDL and service class for saving the file
    @Override
    public OtpResponse initiateKyc(KycInitiationRequest request, MultipartFile file) {
        Customer customer = customerService.getCustomer(request.getCustomerNumber());

        KycDocument kycDocument = new KycDocument();
        kycDocument.setDocumentNumber(request.getDocumentNumber());
        kycDocument.setCustomer(customer);
        kycDocument.setType(KycDocument.DocumentType.valueOf(request.getDocumentType().toUpperCase()));
        KycDocument saved = repository.save(kycDocument);

        String requestId = saved.getId().toString();
        String email = contactService.getCustomerEmail(customer.getId());
        OtpStore otpStore = otpCacheService.generateOtp(requestId, OtpPurpose.KYC_VERIFICATION);
        otpSender.send(email, otpStore.plainOtp());

        return new OtpResponse("KYC initiated. Verify with OTP to complete", requestId);
    }


    @Transactional
    @Override
    public void verifyKyc(String requestId, String otp) {
        otpCacheService.validateOtp(requestId, OtpPurpose.KYC_VERIFICATION, otp);
        updateKycStatus(requestId);
    }

    private void updateKycStatus(String requestId) {
        KycDocument document = repository.findById(UUID.fromString(requestId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reinitiate Kyc")
        );

        document.setVerifiedBy(KycDocument.KycVerification.SYSTEM_UIDAI);
        repository.save(document);
        String userId = jwtTokenService.getUserId();
        authServerClient.updateUserStatus(userId, CustomerStatus.ACTIVE.toString());
        customerService.updateKycStaus(document.getCustomer().getCustomerNumber(), KycStatus.VERIFIED);
    }
}
