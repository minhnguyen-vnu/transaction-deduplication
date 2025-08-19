package com.example.order_service.core.domain.dto.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateOrderDTO {
    private Long userId;
    private BigDecimal amount;
}
