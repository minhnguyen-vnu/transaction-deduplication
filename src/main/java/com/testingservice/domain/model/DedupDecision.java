package com.testingservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DedupDecision {
    private String decision; // "ALLOW" | "DUPLICATE"
    private String reason;
}
