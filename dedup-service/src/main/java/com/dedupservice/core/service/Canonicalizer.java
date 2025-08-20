package com.dedupservice.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Canonicalizer {
    public String buildCanonical(JsonNode payload, List<String> idempotentFields) {
        if (payload == null) throw new IllegalArgumentException("requestPayload is required");
        if (idempotentFields == null || idempotentFields.isEmpty()) {
            throw new IllegalArgumentException("Either idempotentKey must be provided or idempotentFields must be non-empty");
        }


        Map<String, String> kv = new TreeMap<>();
        for (String path : idempotentFields) {
            if (path == null || path.isBlank()) continue;
            Optional<JsonNode> nodeOpt = getByPath(payload, path);
            if (nodeOpt.isEmpty() || nodeOpt.get().isNull()) {
                kv.put(path, "");
            } else {
                kv.put(path, normalize(nodeOpt.get()));
            }
        }


        return kv.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("|"));
    }


    private static Optional<JsonNode> getByPath(JsonNode root, String path) {
        String[] parts = path.split("\\.");
        JsonNode cur = root;
        for (String p : parts) {
            if (cur == null) return Optional.empty();
            cur = cur.get(p);
            if (cur == null) return Optional.empty();
        }
        return Optional.ofNullable(cur);
    }


    private static String normalize(JsonNode n) {
        if (n.isTextual()) {
            return n.asText().trim();
        } else if (n.isNumber()) {
            BigDecimal bd = n.isFloatingPointNumber()
                    ? new BigDecimal(n.asText())
                    : new BigDecimal(n.asLong());
            return bd.stripTrailingZeros().toPlainString();
        } else if (n.isBoolean()) {
            return n.asBoolean() ? "true" : "false";
        } else if (n.isArray()) {
            List<String> items = new ArrayList<>();
            n.forEach(child -> items.add(normalize(child)));
            return String.join(",", items);
        } else if (n.isObject()) {
            List<String> pairs = new ArrayList<>();
            n.fieldNames().forEachRemaining(fn -> {
                JsonNode v = n.get(fn);
                pairs.add(fn + ":" + normalize(v));
            });
            Collections.sort(pairs);
            return "{" + String.join(",", pairs) + "}";
        } else if (n.isNull()) {
            return "";
        }
        return n.asText();
    }
}