package com.example.order_service.core.service.impl;


import com.example.order_service.core.domain.constants.OrderStatus;
import com.example.order_service.core.domain.dto.order.CreateOrUpdateOrderDTO;
import com.example.order_service.core.domain.dto.order.OrderDTO;
import com.example.order_service.core.domain.entity.OrderEntity;
import com.example.order_service.core.port.messaging.MessagingPort;
import com.example.order_service.core.port.store.OrderStore;
import com.example.order_service.core.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderStore orderStore;
    private final MessagingPort messagingPort;

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

        messagingPort.publishOrderCreatedEvent("Order created: " + saved.getId());

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

