package com.example.shipment_service.core.service.impl;

import com.example.shipment_service.core.domain.constants.ShipmentStatus;
import com.example.shipment_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.shipment_service.core.domain.dto.shipment.ShipmentDTO;
import com.example.shipment_service.core.domain.entity.ShipmentEntity;
import com.example.shipment_service.core.port.store.ShipmentStore;
import com.example.shipment_service.core.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentStore shipmentStore;

    @Override
    public ShipmentDTO createShipment(CreateOrUpdateShipmentDTO dto) {
        ShipmentEntity entity = ShipmentEntity.builder()
                .orderId(dto.getOrderId())
                .shippingAddress(dto.getShippingAddress())
                .status(ShipmentStatus.PENDING)
                .timestamp(System.currentTimeMillis())
                .nonce(ThreadLocalRandom.current().nextLong())
                .build();
        ShipmentEntity saved = shipmentStore.save(entity);
        return mapToDto(saved);
    }

    @Override
    public ShipmentDTO getById(Long id) {
        return shipmentStore.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    public List<ShipmentDTO> getAll() {
        return shipmentStore.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ShipmentDTO updateShipment(Long id, CreateOrUpdateShipmentDTO dto) {
        ShipmentEntity entity = shipmentStore.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        entity.setOrderId(dto.getOrderId());
        entity.setShippingAddress(dto.getShippingAddress());

        ShipmentEntity updated = shipmentStore.save(entity);
        return mapToDto(updated);
    }

    @Override
    public void deleteShipment(Long id) {
        shipmentStore.deleteById(id);
    }

    private ShipmentDTO mapToDto(ShipmentEntity e) {
        return ShipmentDTO.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .shippingAddress(e.getShippingAddress())
                .status(e.getStatus().name())
                .timestamp(e.getTimestamp())
                .nonce(e.getNonce())
                .build();
    }
}
