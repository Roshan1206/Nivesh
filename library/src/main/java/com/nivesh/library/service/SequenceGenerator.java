package com.nivesh.library.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

/**
 * Generate next value based on given sequence
 *
 * @author Roshan
 */
@Component
public class SequenceGenerator {

    /**
     * To Perform in db with queries
     */
    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Injecting required dependency
     */
    public SequenceGenerator(EntityManager entityManager){
        this.entityManager = entityManager;
    }


    /**
     * Get next value from the sequence
     *
     * @param seq sequence name
     * @return next value
     */
    public long generateNextSeqValue(String seq) {
        String sequence = "SELECT nextVal('" + seq + "')";
        return ((Number) entityManager
                .createNativeQuery(sequence)
                .getSingleResult())
                .longValue();
    }
}
