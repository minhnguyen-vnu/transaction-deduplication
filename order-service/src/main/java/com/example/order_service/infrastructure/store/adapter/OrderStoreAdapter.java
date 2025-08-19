package com.example.order_service.infrastructure.store.adapter;

import com.example.order_service.core.domain.entity.OrderEntity;
import com.example.order_service.core.port.store.OrderStore;
import com.example.order_service.infrastructure.store.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderStoreAdapter implements OrderStore {

    private final OrderRepository orderRepository;

    @Override
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<OrderEntity> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<OrderEntity> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        orderRepository.deleteById(id);
    }
}