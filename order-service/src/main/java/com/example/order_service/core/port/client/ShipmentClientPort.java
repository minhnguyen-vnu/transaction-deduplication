package com.example.order_service.core.port.client;


import com.example.order_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.order_service.core.domain.dto.shipment.ShipmentDTO;

public interface ShipmentClientPort {
    ShipmentDTO createShipment(CreateOrUpdateShipmentDTO dto);
}
