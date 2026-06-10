package com.nivesh.library.constant;

public final class KafkaTopics {

    private KafkaTopics() {}

//    public static final String CREDIT_FAILED = "transaction.credit.failed";
    public static final String COMPENSATE_REQUEST = "transaction.compensate.request";
    public static final String COMPENSATE_SUCCESS = "transaction.compensate.success";
    public static final String COMPENSATE_FAILED = "transaction.compensate.failed";
    public static final String DEAD_LETTER = "transaction.dead.letter";
}
