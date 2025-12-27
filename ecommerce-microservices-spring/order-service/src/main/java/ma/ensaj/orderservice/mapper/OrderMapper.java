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
        response.setPrice(entity.getPrice());
        response.setQuantity(entity.getQuantity());
        return response;
    }

    public OrderResponse toOrderResponse(Order entity) {
        OrderResponse response = new OrderResponse();
        response.setId(entity.getId());
        response.setOrderNumber(entity.getOrderNumber());
        response.setOrderLines(
                entity.getOrderLines().stream()
                        .map(this::toOrderLineResponse)
                        .collect(Collectors.toList())
        );
        return response;
    }
}
