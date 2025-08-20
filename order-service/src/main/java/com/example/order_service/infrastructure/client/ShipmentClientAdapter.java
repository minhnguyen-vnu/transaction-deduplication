package com.example.order_service.infrastructure.client;

import com.example.order_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.order_service.core.domain.dto.shipment.ShipmentDTO;
import com.example.order_service.core.port.client.ShipmentClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ShipmentClientAdapter implements ShipmentClientPort {

    private final RestTemplate restTemplate;

    @Value("${shipment.base-url}")
    private String shipmentBaseUrl;

    @Override
    public ShipmentDTO createShipment(CreateOrUpdateShipmentDTO dto) {
        String url = shipmentBaseUrl + "/api/shipments";
        return restTemplate.postForObject(url, dto, ShipmentDTO.class);
    }
}

