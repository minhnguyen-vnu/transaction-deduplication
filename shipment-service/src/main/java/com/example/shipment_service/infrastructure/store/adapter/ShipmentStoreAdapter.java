package com.example.shipment_service.infrastructure.store.adapter;

import com.example.shipment_service.core.domain.entity.ShipmentEntity;
import com.example.shipment_service.core.port.store.ShipmentStore;
import com.example.shipment_service.infrastructure.store.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ShipmentStoreAdapter implements ShipmentStore {

    private final ShipmentRepository shipmentRepository;

    @Override
    public ShipmentEntity save(ShipmentEntity shipment) {
        return shipmentRepository.save(shipment);
    }

    @Override
    public Optional<ShipmentEntity> findById(Long id) {
        return shipmentRepository.findById(id);
    }

    @Override
    public List<ShipmentEntity> findAll() {
        return shipmentRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        shipmentRepository.deleteById(id);
    }
}
