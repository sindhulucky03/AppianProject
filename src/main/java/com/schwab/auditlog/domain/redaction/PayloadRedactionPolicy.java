package com.schwab.auditlog.domain.redaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schwab.auditlog.config.AuditRedactionProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PayloadRedactionPolicy {

    private final Set<String> sensitiveFieldNames;

    public PayloadRedactionPolicy(AuditRedactionProperties properties) {
        this.sensitiveFieldNames = properties.sensitiveFieldNames().stream()
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public PayloadProjection project(JsonNode original) {
        List<SensitivePayloadValue> sensitiveValues = new ArrayList<>();
        return new PayloadProjection(projectNode(original, "", sensitiveValues), List.copyOf(sensitiveValues));
    }

    private JsonNode projectNode(JsonNode node, String pointer, List<SensitivePayloadValue> sensitiveValues) {
        if (node.isObject()) {
            ObjectNode projected = JsonNodeFactory.instance.objectNode();
            node.fields().forEachRemaining(field -> {
                String childPointer = pointer + "/" + escape(field.getKey());
                if (sensitiveFieldNames.contains(field.getKey().toLowerCase(Locale.ROOT))) {
                    sensitiveValues.add(new SensitivePayloadValue(childPointer, field.getValue().deepCopy()));
                    projected.put(field.getKey(), "[REDACTED]");
                } else {
                    projected.set(field.getKey(), projectNode(field.getValue(), childPointer, sensitiveValues));
                }
            });
            return projected;
        }
        if (node.isArray()) {
            ArrayNode projected = JsonNodeFactory.instance.arrayNode();
            for (int index = 0; index < node.size(); index++) {
                projected.add(projectNode(node.get(index), pointer + "/" + index, sensitiveValues));
            }
            return projected;
        }
        return node.deepCopy();
    }

    private String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }
}
