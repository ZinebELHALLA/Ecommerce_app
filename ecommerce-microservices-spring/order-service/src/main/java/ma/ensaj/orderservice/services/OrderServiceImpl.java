package ma.ensaj.orderservice.services;

import ma.ensaj.orderservice.dtos.OrderRequest;
import ma.ensaj.orderservice.dtos.OrderResponse;
import ma.ensaj.orderservice.entities.Order;
import ma.ensaj.orderservice.mapper.OrderMapper;
import ma.ensaj.orderservice.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        Order order = mapper.toOrderEntity(request);

        // generate unique number
        order.setOrderNumber(UUID.randomUUID().toString());

        Order savedOrder = orderRepository.save(order);

        return mapper.toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return mapper.toOrderResponse(order);
    }
}
