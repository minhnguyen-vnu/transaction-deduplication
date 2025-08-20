package com.example.order_service.core.service.impl;


import com.example.order_service.core.domain.constants.OrderStatus;
import com.example.order_service.core.domain.constants.RequestStatus;
import com.example.order_service.core.domain.dto.integration.IntegrationMessageDTO;
import com.example.order_service.core.domain.dto.order.CreateOrUpdateOrderDTO;
import com.example.order_service.core.domain.dto.order.OrderDTO;
import com.example.order_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.order_service.core.domain.entity.OrderEntity;
import com.example.order_service.core.port.client.ShipmentClientPort;
import com.example.order_service.core.port.messaging.MessagingPort;
import com.example.order_service.core.port.store.OrderStore;
import com.example.order_service.core.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderStore orderStore;
    private final MessagingPort messagingPort;
    private final ShipmentClientPort shipmentClientPort;   // thêm vào constructor


    @Override
    public OrderDTO createOrder(CreateOrUpdateOrderDTO dto) {
        String code = "ORDER_" + ThreadLocalRandom.current().nextLong();
        OrderEntity entity = OrderEntity.builder()
                .code(code)
                .userId(dto.getUserId())
                .amount(dto.getAmount())
                .status(OrderStatus.NEW)
                .timestamp(System.currentTimeMillis())
                .nonce(ThreadLocalRandom.current().nextLong())
                .build();

        OrderEntity saved = orderStore.save(entity);

        // Tạo requestId & idempotentKey
        String requestId = UUID.randomUUID().toString();
        String idemKey = UUID.randomUUID().toString();

        // Publish kafka PENDING
        IntegrationMessageDTO pendingMsg = IntegrationMessageDTO.builder()
                .requestId(requestId)
                .sourceService("ORDER")
                .targetService("SHIPMENT")
                .method("createShipment")
                .idempotentKey(idemKey)
                .status(RequestStatus.PENDING)
                .payload(dto)  // payload chưa có shipment, dùng OrderDTO hoặc Shipment DTO tùy bạn
                .build();
        messagingPort.publishOrderCreatedEvent(pendingMsg.toString()); // (Có thể JSON stringify tùy config)

        try {
            // Gọi shipment service
            CreateOrUpdateShipmentDTO shipmentDto = CreateOrUpdateShipmentDTO.builder()
                    .orderId(saved.getId())
                    .shippingAddress("Shipping address") // Giả sử dto có trường này
                    .timestamp(System.currentTimeMillis())
                    .nonce(ThreadLocalRandom.current().nextLong())
                    .build();
            shipmentClientPort.createShipment(shipmentDto);

            // Publish kafka SUCCESS
            IntegrationMessageDTO successMsg = IntegrationMessageDTO.builder()
                    .requestId(requestId)
                    .sourceService("ORDER")
                    .targetService("SHIPMENT")
                    .method("POST")
                    .endpoint("/api/shipments")
                    .idempotentKey(idemKey)
                    .status(RequestStatus.SUCCESS)
                    .payload(shipmentDto)
                    .build();
            messagingPort.publishOrderCreatedEvent(successMsg.toString());

        } catch (Exception e) {
            // Publish FAILED
            IntegrationMessageDTO failedMsg = IntegrationMessageDTO.builder()
                    .requestId(requestId)
                    .sourceService("ORDER")
                    .targetService("SHIPMENT")
                    .method("createShipment")
                    .idempotentKey(idemKey)
                    .status(RequestStatus.FAILED)
                    .payload(dto)
                    .build();
            messagingPort.publishOrderCreatedEvent(failedMsg.toString());
            throw e;
        }

        return mapToDto(saved);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        return orderStore.findById(id).map(this::mapToDto).orElse(null);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderStore.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public OrderDTO updateOrder(Long id, CreateOrUpdateOrderDTO dto) {
        OrderEntity entity = orderStore.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        entity.setUserId(dto.getUserId());
        entity.setAmount(dto.getAmount());

        OrderEntity saved = orderStore.save(entity);
        return mapToDto(saved);
    }

    @Override
    public void deleteOrder(Long id) {
        orderStore.deleteById(id);
    }

    private OrderDTO mapToDto(OrderEntity e) {
        return OrderDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .userId(e.getUserId())
                .amount(e.getAmount())
                .status(e.getStatus().name())
                .timestamp(e.getTimestamp())
                .nonce(e.getNonce())
                .build();
    }
}

