package com.example.shipment_service.core.domain.dto.shipment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateShipmentDTO {
    private Long orderId;
    private String shippingAddress;
    private Long timestamp;
    private Long nonce;
}