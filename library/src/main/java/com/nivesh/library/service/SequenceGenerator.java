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
     * Retrieves the next sequence value from the database.
     * Used for generating unique IDs.
     *
     * @param seq the name of the database sequence
     * @return the next value from the sequence
     */
    public long generateNextSeqValue(String seq) {
        String sequence = "SELECT nextVal('" + seq + "')";
        return ((Number) entityManager
                .createNativeQuery(sequence)
                .getSingleResult())
                .longValue();
    }
}
