package com.example.order_service.core.domain.dto.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String code;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private Long timestamp;
    private Long nonce;
}