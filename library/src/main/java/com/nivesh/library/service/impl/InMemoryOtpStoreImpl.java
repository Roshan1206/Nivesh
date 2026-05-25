package com.nivesh.library.service.impl;

import com.nivesh.library.dto.OtpRecord;
import com.nivesh.library.service.OtpStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory otp store for validating otp locally.
 *
 * @author Roshan
 */
@Profile("!prod")
@Service
public class InMemoryOtpStoreImpl implements OtpStore {

    /**
     * otp expiry time
     */
    @Value("${nivesh.otp.expiry-in-min}")
    public String expiry;

    /**
     * Stores the otp in ConcurrentHashMap
     */
    private final Map<String, OtpRecord> otpRecord = new ConcurrentHashMap<>();


    /**
     * Saves the otp in map
     */
    @Override
    public void save(String identifier, String otp, String customerNumber) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(Long.parseLong(expiry));
        otpRecord.put(identifier, new OtpRecord(otp, expiresAt, customerNumber));
    }

    /**
     * Verify the otp.
     */
    @Override
    public String verify(String identifier, String otp) {
        OtpRecord record = otpRecord.get(identifier);

        if (record == null || record.isExpired()) {
            otpRecord.remove(identifier);
            return null;
        }
        if (record.otp().equals(otp)){
            otpRecord.remove(identifier);
            return record.customerNumber();
        }
        return null;
    }
}
