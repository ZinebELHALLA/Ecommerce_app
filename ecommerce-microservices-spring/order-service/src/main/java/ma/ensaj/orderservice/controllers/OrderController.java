package ma.ensaj.orderservice.controllers;

import ma.ensaj.orderservice.dtos.OrderRequest;
import ma.ensaj.orderservice.dtos.OrderResponse;
import ma.ensaj.orderservice.services.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
