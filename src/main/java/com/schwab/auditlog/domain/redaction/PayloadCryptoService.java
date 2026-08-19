package com.schwab.auditlog.domain.redaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schwab.auditlog.config.AuditRedactionProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PayloadCryptoService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final ObjectMapper objectMapper;
    private final byte[] masterKey;
    private final SecureRandom random = new SecureRandom();

    public PayloadCryptoService(ObjectMapper objectMapper, AuditRedactionProperties properties) {
        this.objectMapper = objectMapper;
        this.masterKey = Base64.getDecoder().decode(properties.masterKey());
        if (masterKey.length != KEY_BYTES) {
            throw new IllegalStateException("audit.redaction.master-key must be a base64-encoded 256-bit key");
        }
    }

    public EncryptedPayload encrypt(JsonNode value) {
        try {
            byte[] dataKey = randomBytes(KEY_BYTES);
            byte[] payloadIv = randomBytes(IV_BYTES);
            byte[] keyIv = randomBytes(IV_BYTES);
            byte[] plaintext = objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
            return new EncryptedPayload(encrypt(plaintext, dataKey, payloadIv), payloadIv,
                    encrypt(dataKey, masterKey, keyIv), keyIv);
        } catch (GeneralSecurityException | JsonProcessingException exception) {
            throw new IllegalStateException("Unable to encrypt sensitive audit payload", exception);
        }
    }

    private byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AES_GCM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
        return cipher.doFinal(plaintext);
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}
