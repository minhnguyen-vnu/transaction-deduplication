package com.example.shipment_service.core.port.store;

import com.example.shipment_service.core.domain.entity.ShipmentEntity;

import java.util.List;
import java.util.Optional;

public interface ShipmentStore {
    ShipmentEntity save(ShipmentEntity shipment);
    Optional<ShipmentEntity> findById(Long id);
    List<ShipmentEntity> findAll();
    void deleteById(Long id);
}