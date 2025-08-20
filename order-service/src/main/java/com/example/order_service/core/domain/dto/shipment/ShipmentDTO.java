package com.example.order_service.core.domain.dto.shipment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDTO {
    private Long id;
    private Long orderId;
    private String shippingAddress;
    private String status;
    private Long timestamp;
    private Long nonce;
}

