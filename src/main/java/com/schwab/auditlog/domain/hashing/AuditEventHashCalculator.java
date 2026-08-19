package com.schwab.auditlog.domain.hashing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class AuditEventHashCalculator {

    private final CanonicalJson canonicalJson;

    public AuditEventHashCalculator(CanonicalJson canonicalJson) {
        this.canonicalJson = canonicalJson;
    }

    public String payloadCommitment(JsonNode originalPayload) {
        return sha256(canonicalJson.write(originalPayload));
    }

    public String eventHash(AuditEventHashMaterial material) {
        ObjectNode preimage = JsonNodeFactory.instance.objectNode();
        preimage.put("eventId", material.eventId().toString());
        preimage.put("eventType", material.eventType());
        preimage.put("actorId", material.actorId());
        preimage.put("resourceType", material.resourceType());
        preimage.put("resourceId", material.resourceId());
        preimage.put("occurredAt", material.occurredAt().toString());
        preimage.set("payloadProjection", material.payloadProjection());
        preimage.put("payloadCommitment", material.payloadCommitment());
        preimage.put("previousHash", material.previousHash());
        return sha256(canonicalJson.write(preimage));
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance(HashConstants.SHA_256)
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 must be available in the JDK", exception);
        }
    }
}
