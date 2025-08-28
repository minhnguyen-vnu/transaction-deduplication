package com.example.order_service.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DedupUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static ObjectNode canonicalize(JsonNode payload,
                                           List<String> idempotentFields,
                                           List<String> ignoredFields) {
        ObjectNode result = mapper.createObjectNode();

        if (idempotentFields != null && !idempotentFields.isEmpty()) {
            for (String f : idempotentFields) {
                if (payload.has(f)) result.set(f, payload.get(f));
            }
        } else {
            Set<String> ignored = (ignoredFields == null) ? Set.of() : new HashSet<>(ignoredFields);
            Iterator<String> it = payload.fieldNames();
            while (it.hasNext()) {
                String f = it.next();
                if (!ignored.contains(f)) result.set(f, payload.get(f));
            }
        }
        return result;
    }

    /** Chuỗi ổn định: sort field theo alphabet để tránh khác biệt thứ tự key của JSON. */
    private static String toDeterministicString(ObjectNode node) {
        List<String> names = new ArrayList<>();
        node.fieldNames().forEachRemaining(names::add);
        Collections.sort(names);
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < names.size(); i++) {
            String k = names.get(i);
            sb.append("\"").append(k).append("\":").append(node.get(k).toString());
            if (i < names.size() - 1) sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot hash input", e);
        }
    }

    /**
     * Sinh key idempotent:
     *  - Payload đã canonicalize (theo idempotentFields hoặc bỏ ignoredFields)
     *  - Time-bucket: floor(now / idempotentWindowMs)
     */
    public static String generateIdempotentKey(JsonNode payload,
                                               List<String> idempotentFields,
                                               List<String> ignoredFields,
                                               long idempotentWindowMs) {
        ObjectNode canonical = canonicalize(payload, idempotentFields, ignoredFields);
        String canonicalStr  = toDeterministicString(canonical); // sắp xếp key để ổn định

        long bucket = idempotentWindowMs <= 0 ? 0 : (System.currentTimeMillis() / idempotentWindowMs);
        log.info("Bucket: {}", bucket);
        String material = canonicalStr + "|" + bucket;

        return "idem:" + sha256Hex(material);
    }
}
