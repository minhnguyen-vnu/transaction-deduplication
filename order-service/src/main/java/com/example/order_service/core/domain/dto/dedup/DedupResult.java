package com.example.order_service.core.domain.dto.dedup;

import com.example.order_service.core.domain.constants.DedupDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupResult {
    public DedupDecision decision;
    public String reason;
}
