package com.example.order_service.infrastructure.store.repository;


import com.example.order_service.core.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}