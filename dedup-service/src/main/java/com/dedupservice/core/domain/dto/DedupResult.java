package com.dedupservice.core.domain.dto;

import com.dedupservice.core.domain.constants.DedupDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupResult {
    private DedupDecision decision; // ALLOW | DUPLICATE | REJECT
    private String reason;
    public static DedupResult allow(){ return DedupResult.builder().decision(DedupDecision.ALLOW).build(); }
    public static DedupResult duplicate(String r){ return DedupResult.builder().decision(DedupDecision.DUPLICATE).reason(r).build(); }
    public static DedupResult reject(String r){ return DedupResult.builder().decision(DedupDecision.REJECT).reason(r).build(); }
}