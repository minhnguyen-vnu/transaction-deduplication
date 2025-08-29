package com.trackingservice.kernel.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingservice.core.domain.entity.StatusRecord;
import com.trackingservice.infrastructure.store.StatusRecordDocument;

import java.lang.reflect.Type;
import java.util.Map;

public class StatusRecordMapper {
    static final ObjectMapper M = new ObjectMapper();

    private StatusRecordMapper(){}

    public static StatusRecord toDomain(StatusRecordDocument d) {
        if (d == null) return null;
        return StatusRecord.builder()
                .idempotentKey(d.getIdempotentKey())
                .requestId(d.getRequestId())
                .sourceService(d.getSourceService())
                .targetService(d.getTargetService())
                .status(d.getStatus())
                .error(d.getError())
                .details(M.valueToTree(d.getDetails()))
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public static StatusRecordDocument toDoc(StatusRecord s) {
        if (s == null) return null;
        return StatusRecordDocument.builder()
                .idempotentKey(s.getIdempotentKey())
                .requestId(s.getRequestId())
                .sourceService(s.getSourceService())
                .targetService(s.getTargetService())
                .status(s.getStatus())
                .error(s.getError())
                .details(M.convertValue(s.getDetails(), new TypeReference<Map<String, Object>>(){}))
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
