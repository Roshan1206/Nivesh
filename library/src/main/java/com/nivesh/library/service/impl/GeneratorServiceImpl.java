package com.nivesh.library.service.impl;

import com.nivesh.library.service.GeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Service class for generating values.
 *
 * @author Roshan
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    /**
     * To Perform in db with queries
     */
    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Injecting required dependency
     */
    public GeneratorServiceImpl(EntityManager entityManager){
        this.entityManager = entityManager;
    }


    /**
     * Get next value from the sequence
     *
     * @param seq sequence name
     * @return next value
     */
    @Override
    public long generateNextSeqValue(String seq) {
        String sequence = "SELECT nextVal('" + seq + "')";
        return ((Number) entityManager
                .createNativeQuery(sequence)
                .getSingleResult())
                .longValue();
    }


    /**
     * Generate otp for verification
     *
     * @return otp
     */
    @Override
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
