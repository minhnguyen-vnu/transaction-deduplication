package com.example.shipment_service.infrastructure.store.repository;

import com.example.shipment_service.core.domain.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {
}