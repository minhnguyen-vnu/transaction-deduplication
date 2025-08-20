package com.testing_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunRequest {
    private int threads = 3;
    private int perThread = 5;
    private long paceMillis = 80;
}
