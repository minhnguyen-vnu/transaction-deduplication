package com.example.order_service.core.port.store;


import com.example.order_service.core.domain.entity.OrderEntity;

import java.util.List;
import java.util.Optional;

public interface OrderStore {
    OrderEntity save(OrderEntity order);
    Optional<OrderEntity> findById(Long id);
    List<OrderEntity> findAll();
    void deleteById(Long id);
}