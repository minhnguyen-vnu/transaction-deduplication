package com.example.order_service.core.service.impl;


import com.example.order_service.core.domain.constants.DedupDecision;
import com.example.order_service.core.domain.constants.OrderStatus;
import com.example.order_service.core.domain.constants.RequestStatus;
import com.example.order_service.core.domain.dto.dedup.DedupRequest;
import com.example.order_service.core.domain.dto.dedup.DedupResult;
import com.example.order_service.core.domain.dto.integration.MessageDTO;
import com.example.order_service.core.domain.dto.order.CreateOrUpdateOrderDTO;
import com.example.order_service.core.domain.dto.order.OrderDTO;
import com.example.order_service.core.domain.dto.shipment.CreateOrUpdateShipmentDTO;
import com.example.order_service.core.domain.entity.OrderEntity;
import com.example.order_service.core.port.client.DedupClientPort;
import com.example.order_service.core.port.client.ShipmentClientPort;
import com.example.order_service.core.port.messaging.MessagingPort;
import com.example.order_service.core.port.store.OrderStore;
import com.example.order_service.core.service.OrderService;
import com.example.order_service.core.utils.DedupUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderStore orderStore;
    private final MessagingPort messagingPort;
    private final DedupClientPort dedupClientPort;
    private final ShipmentClientPort shipmentClientPort;   // thêm vào constructor
    private final ObjectMapper mapper;

    @Value("${dedup.window-ms}")
    private long idempotentWindowMs;

    @Override
    public OrderDTO createOrder(CreateOrUpdateOrderDTO dto) {
        // 1. Tạo Order entity và lưu DB
        OrderEntity saved = createAndSaveOrder(dto);

        // 2. Chuẩn bị idempotent key
        List<String> idempotentFields = List.of("userId", "amount");
        List<String> ignoredFields    = List.of("timestamp", "nonce");
        JsonNode payloadNode          = mapper.valueToTree(dto);

        String idemKey   = DedupUtil.generateIdempotentKey(payloadNode, idempotentFields, ignoredFields, idempotentWindowMs);
        String requestId = UUID.randomUUID().toString();

        // 3. Build DedupRequest đầy đủ
        DedupRequest dedupRequest = DedupRequest.builder()
                .requestId(requestId)
                .sourceService("ORDER")
                .targetService("DEDUP")
                .method("POST")
                .endpoint("/dedup/check")
                .requestPayload(payloadNode)
                .idempotentKey(idemKey)
                .idempotentFields(idempotentFields)
                .ignoredFields(ignoredFields)
                .build();

        // 4) Gọi Dedup Support + đo thời gian phản hồi
        long start = System.currentTimeMillis();
        DedupResult response = dedupClientPort.checkDedup(dedupRequest);
        if (response.getDecision() != DedupDecision.ALLOW) {
            log.warn("Dedup check failed for key={}, decision={}", idemKey, response.getDecision());
            throw new RuntimeException("Order creation is not allowed due to deduplication rules.");
        }
        log.info("Dedup check result: {}", response);
        long elapsed = System.currentTimeMillis() - start;

        log.info("Dedup check completed in {} ms, key={}", elapsed, idemKey);

        try {
            // 5) Gọi shipment service
            CreateOrUpdateShipmentDTO shipmentDto = buildShipmentRequest(saved);
            shipmentClientPort.createShipment(shipmentDto);

            // 6. Publish SUCCESS
            publishEvent(dedupRequest, RequestStatus.SUCCESS);

        } catch (Exception e) {
            // Publish FAILED
            publishEvent(dedupRequest, RequestStatus.FAILED);
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

    private OrderEntity createAndSaveOrder(CreateOrUpdateOrderDTO dto) {
        OrderEntity entity = OrderEntity.builder()
                .code("ORDER_" + ThreadLocalRandom.current().nextLong())
                .userId(dto.getUserId())
                .amount(dto.getAmount())
                .status(OrderStatus.NEW)
                .timestamp(System.currentTimeMillis())
                .nonce(ThreadLocalRandom.current().nextLong())
                .build();
        return orderStore.save(entity);
    }

    private void publishEvent(DedupRequest request, RequestStatus status) {
        MessageDTO msg = MessageDTO.builder()
                .requestId(request.getRequestId())
                .sourceService(request.getSourceService())
                .targetService(request.getTargetService())
                .method(request.getMethod())
                .endpoint(request.getEndpoint())
                .idempotentKey(request.getIdempotentKey())
                .status(status) // lấy từ tham số hàm
                .payload(request.getRequestPayload())
                .build();

        messagingPort.publishOrderCreatedEvent(msg.toString()); // JSON stringify nếu cần
    }

    private CreateOrUpdateShipmentDTO buildShipmentRequest(OrderEntity saved) {
        return CreateOrUpdateShipmentDTO.builder()
                .orderId(saved.getId())
                .shippingAddress("Default address")
                .timestamp(System.currentTimeMillis())
                .nonce(ThreadLocalRandom.current().nextLong())
                .build();
    }
}

