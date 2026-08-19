package com.schwab.auditlog.domain.hashing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Component;

/** Produces the one documented JSON representation used in every hash preimage. */
@Component
public class CanonicalJson {

    private final ObjectMapper objectMapper;

    public CanonicalJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String write(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(normalize(node));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to canonicalize JSON", exception);
        }
    }

    private JsonNode normalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode normalized = JsonNodeFactory.instance.objectNode();
            Map<String, JsonNode> sortedFields = new TreeMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            fields.forEachRemaining(field -> sortedFields.put(field.getKey(), field.getValue()));
            sortedFields.forEach((name, value) -> normalized.set(name, normalize(value)));
            return normalized;
        }
        if (node.isArray()) {
            ArrayNode normalized = JsonNodeFactory.instance.arrayNode();
            node.forEach(value -> normalized.add(normalize(value)));
            return normalized;
        }
        if (node.isNumber()) {
            BigDecimal normalizedNumber = node.decimalValue().stripTrailingZeros();
            return DecimalNode.valueOf(normalizedNumber.signum() == 0 ? BigDecimal.ZERO : normalizedNumber);
        }
        return node;
    }
}
