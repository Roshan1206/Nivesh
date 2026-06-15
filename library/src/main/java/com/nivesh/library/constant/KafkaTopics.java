package com.nivesh.library.constant;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String DEBIT_REQUESTED = "transaction.debit.requested";
    public static final String DEBIT_RESULT = "transaction.debit.result";
    public static final String CREDIT_REQUESTED = "transaction.credit.requested";
    public static final String CREDIT_RESULT = "transaction.credit.result";
    public static final String TRANSFER_REQUESTED = "transaction.transfer.request";
    public static final String TRANSFER_RESULT = "transaction.transfer.result";
    public static final String COMPENSATE_REQUEST = "transaction.compensate.request";
    public static final String COMPENSATE_SUCCESS = "transaction.compensate.success";
    public static final String COMPENSATE_FAILED = "transaction.compensate.failed";
    public static final String DEAD_LETTER = "transaction.dead.letter";
}
