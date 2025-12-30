package ma.ensaj.orderservice.mapper;

import ma.ensaj.orderservice.dtos.OrderLineRequest;
import ma.ensaj.orderservice.dtos.OrderLineResponse;
import ma.ensaj.orderservice.dtos.OrderRequest;
import ma.ensaj.orderservice.dtos.OrderResponse;
import ma.ensaj.orderservice.entities.Order;
import ma.ensaj.orderservice.entities.OrderLine;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
@Component
public class OrderMapper {
    public OrderLine toOrderLineEntity(OrderLineRequest request) {
        OrderLine orderLine = new OrderLine();
        orderLine.setSkuCode(request.getSkuCode());
        orderLine.setPrice(request.getPrice());
        orderLine.setQuantity(request.getQuantity());
        return orderLine;
    }

    public Order toOrderEntity(OrderRequest request) {
        Order order = new Order();
        order.setOrderLines(
                request.getOrderLines()
                        .stream()
                        .map(this::toOrderLineEntity)
                        .collect(Collectors.toList())
        );
        return order;
    }

    public OrderLineResponse toOrderLineResponse(OrderLine entity) {
        OrderLineResponse response = new OrderLineResponse();
        response.setId(entity.getId());
        response.setSkuCode(entity.getSkuCode());
        response.setProductName(entity.getSkuCode()); // Use SKU as product name for now
        response.setPrice(entity.getPrice());
        response.setQuantity(entity.getQuantity());
        response.setTotalPrice(entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())));
        return response;
    }

    public OrderResponse toOrderResponse(Order entity) {
        OrderResponse response = new OrderResponse();
        response.setId(entity.getId());
        response.setOrderNumber(entity.getOrderNumber());
        response.setUserId(entity.getClientId());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : "PENDING");
        response.setTotalAmount(entity.getTotalAmount());
        response.setDeliveryAddress(entity.getDeliveryAddress());
        response.setPaymentMethod(entity.getPaymentMethod());
        response.setOrderDate(entity.getCreatedAt());
        if (entity.getOrderLines() != null && !entity.getOrderLines().isEmpty()) {
            response.setItems(
                    entity.getOrderLines().stream()
                            .map(this::toOrderLineResponse)
                            .collect(Collectors.toList())
            );
        } else {
            response.setItems(new java.util.ArrayList<>());
        }
        return response;
    }
}
