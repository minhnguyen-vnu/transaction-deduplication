package com.example.shipment_service.core.service;

import com.example.shipment_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.shipment_service.core.domain.dto.shipment.ShipmentDTO;

import java.util.List;

public interface ShipmentService {
    ShipmentDTO createShipment(CreateOrUpdateShipmentDTO dto);
    ShipmentDTO getById(Long id);
    List<ShipmentDTO> getAll();
    ShipmentDTO updateShipment(Long id, CreateOrUpdateShipmentDTO dto);
    void deleteShipment(Long id);
}