package com.schwab.auditlog.api.dto;

public enum ChainViolationType {
    PREVIOUS_HASH_MISMATCH,
    CONTENT_HASH_MISMATCH,
    CHAIN_HEAD_SEQUENCE_MISMATCH,
    CHAIN_HEAD_HASH_MISMATCH
}
