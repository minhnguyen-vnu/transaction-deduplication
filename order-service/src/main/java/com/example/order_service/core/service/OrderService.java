package com.example.order_service.core.service;



import com.example.order_service.core.domain.dto.order.CreateOrUpdateOrderDTO;
import com.example.order_service.core.domain.dto.order.OrderDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(CreateOrUpdateOrderDTO dto);
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getAllOrders();
    OrderDTO updateOrder(Long id, CreateOrUpdateOrderDTO dto);
    void deleteOrder(Long id);
}
