package com.dedupservice.core.service;

import com.dedupservice.core.domain.dto.DedupCheckRequest;
import com.dedupservice.core.domain.dto.DedupDecision;
import com.dedupservice.core.port.store.DedupStore;
import com.dedupservice.infrastructure.config.DedupProperties;
import com.dedupservice.infrastructure.store.RedisBloomIdempotencyStore;
import com.dedupservice.kernel.utils.KeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DedupUseCase {


    private final Canonicalizer canonicalizer;
    private final DedupStore store;
    private final DedupProperties props;
    private final RedisBloomIdempotencyStore redisBloomIdempotencyStore;


    public DedupDecision check(DedupCheckRequest req) {
        String keyMaterial;


        if (StringUtils.hasText(req.getIdempotentKey())) {
            keyMaterial = req.getIdempotentKey().trim();
        } else {
            keyMaterial = canonicalizer.buildCanonical(
                    req.getRequestPayload(),
                    req.getIdempotentFields()
            );
        }


        String storeKey = KeyUtil.sha256Hex(keyMaterial);

        boolean doesExist = redisBloomIdempotencyStore.isDuplicate(storeKey);
        if (!doesExist) {
            redisBloomIdempotencyStore.recordProcessed(storeKey);
            return DedupDecision.allow();
        } else {
            boolean ok = store.reserve(storeKey, props.getTtlSeconds());
            return ok ? DedupDecision.allow() : DedupDecision.duplicate("FOUND_IN_STORE");
        }
    }
}
