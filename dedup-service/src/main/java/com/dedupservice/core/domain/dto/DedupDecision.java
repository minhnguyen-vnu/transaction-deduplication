package com.dedupservice.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupDecision {
    private String decision; // ALLOW | DUPLICATE | REJECT
    private String reason;
    public static DedupDecision allow(){ return DedupDecision.builder().decision("ALLOW").build(); }
    public static DedupDecision duplicate(String r){ return DedupDecision.builder().decision("DUPLICATE").reason(r).build(); }
    public static DedupDecision reject(String r){ return DedupDecision.builder().decision("REJECT").reason(r).build(); }
}