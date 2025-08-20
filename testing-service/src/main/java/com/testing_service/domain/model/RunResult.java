package com.testing_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunResult {
    private int attempted;
    private int allowed;
    private int duplicate;
    private int bSuccess;
    private int bTimeout;
}
