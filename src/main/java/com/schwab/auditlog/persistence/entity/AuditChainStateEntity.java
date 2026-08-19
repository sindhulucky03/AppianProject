package com.schwab.auditlog.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_chain_state")
public class AuditChainStateEntity {

    public static final short GLOBAL_CHAIN_ID = 1;

    @Id
    @Column(name = "chain_id")
    private Short chainId;
    @Column(name = "last_sequence", nullable = false)
    private long lastSequence;
    @Column(name = "last_hash", nullable = false)
    private String lastHash;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AuditChainStateEntity() {
    }

    public long getLastSequence() { return lastSequence; }
    public String getLastHash() { return lastHash; }

    public void advance(long sequence, String hash, Instant timestamp) {
        this.lastSequence = sequence;
        this.lastHash = hash;
        this.updatedAt = timestamp;
    }
}
