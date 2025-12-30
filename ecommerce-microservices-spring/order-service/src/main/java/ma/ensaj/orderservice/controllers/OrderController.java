package ma.ensaj.orderservice.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.orderservice.dtos.CheckoutRequest;
import ma.ensaj.orderservice.dtos.OrderRequest;
import ma.ensaj.orderservice.dtos.OrderResponse;
import ma.ensaj.orderservice.services.OrderService;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Slf4j
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
    
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
    
    // New checkout endpoint with complete validation workflow
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout request received for user: {}", request.getUserId());
        
        try {
            OrderResponse orderResponse = orderService.checkout(request);
            log.info("Checkout successful for user: {}, Order: {}", 
                    request.getUserId(), orderResponse.getOrderNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
            
        } catch (Exception e) {
            log.error("Checkout failed for user {}: {}", request.getUserId(), e.getMessage());
            throw e;
        }
    }
}
