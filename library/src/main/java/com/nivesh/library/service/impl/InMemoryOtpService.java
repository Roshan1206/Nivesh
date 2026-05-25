package com.nivesh.library.service.impl;

import com.nivesh.library.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * In memory otp service. otp are logged uin console.
 *
 * @author Roshan
 */
@Slf4j
@Service
public class InMemoryOtpService implements OtpService {

    /**
     * log the otp in console
     */
    @Override
    public void send(String mobile, String otp) {
        log.info("Otp for {}: {}", mobile, otp);
    }
}
