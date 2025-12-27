package ma.ensaj.orderservice.services;

import ma.ensaj.orderservice.dtos.OrderRequest;
import ma.ensaj.orderservice.dtos.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
}
