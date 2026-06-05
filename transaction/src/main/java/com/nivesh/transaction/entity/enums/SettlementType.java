package com.nivesh.transaction.entity.enums;

/**
 * Enumerates supported settlement type values used by the transaction domain model.
 */
public enum SettlementType {

    /** Settlement is completed immediately. */
    INSTANT,

    /** Settlement is completed later through a deferred process. */
    DEFERRED
}
