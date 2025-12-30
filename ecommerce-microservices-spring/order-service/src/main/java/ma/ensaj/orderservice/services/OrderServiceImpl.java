package ma.ensaj.orderservice.services;

import lombok.extern.slf4j.Slf4j;
import ma.ensaj.orderservice.clients.CartClient;
import ma.ensaj.orderservice.clients.ClientClient;
import ma.ensaj.orderservice.clients.InventoryClient;
import ma.ensaj.orderservice.dtos.*;
import ma.ensaj.orderservice.entities.OrderStatus;
import ma.ensaj.orderservice.exceptions.CartEmptyException;
import ma.ensaj.orderservice.exceptions.CheckoutValidationException;
import ma.ensaj.orderservice.exceptions.ClientValidationException;
import ma.ensaj.orderservice.entities.Order;
import ma.ensaj.orderservice.entities.OrderLine;
import ma.ensaj.orderservice.mapper.OrderMapper;
import ma.ensaj.orderservice.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final CartClient cartClient;
    private final ClientClient clientClient;
    private final InventoryClient inventoryClient;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper mapper,
                           CartClient cartClient, ClientClient clientClient,
                           InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.mapper = mapper;
        this.cartClient = cartClient;
        this.clientClient = clientClient;
        this.inventoryClient = inventoryClient;
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
    
    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest checkoutRequest) {
        log.info("Starting checkout process for user: {}", checkoutRequest.getUserId());
        
        List<String> validationErrors = new ArrayList<>();
        
        try {
            // STEP 1: Validate client exists
            log.info("Step 1: Validating client {}", checkoutRequest.getUserId());
            ClientValidationResponse clientValidation = clientClient.validateClient(checkoutRequest.getUserId());
            if (!clientValidation.isExists()) {
                throw new ClientValidationException("Client validation failed: " + clientValidation.getErrorMessage());
            }
            
            // STEP 2: Get validated cart
            log.info("Step 2: Retrieving and validating cart for user {}", checkoutRequest.getUserId());
            CartValidationResponse cartValidation = cartClient.getValidatedCart(checkoutRequest.getUserId());
            
            // Check if cart is empty
            if (cartValidation.getItems() == null || cartValidation.getItems().isEmpty()) {
                throw new CartEmptyException("Cannot checkout with empty cart for user: " + checkoutRequest.getUserId());
            }
            
            // Check cart validation status
            if (!cartValidation.isValid()) {
                validationErrors.addAll(cartValidation.getValidationErrors());
                throw new CheckoutValidationException("Cart validation failed", validationErrors);
            }
            
            // STEP 3: Final inventory check (double-check stock before order creation)
            log.info("Step 3: Final inventory validation for {} items", cartValidation.getItems().size());
            List<BatchInventoryRequest> inventoryRequests = cartValidation.getItems().stream()
                    .map(item -> new BatchInventoryRequest(item.getSkuCode(), item.getQuantity()))
                    .collect(Collectors.toList());
            
            List<InventoryValidationResponse> finalStockCheck = inventoryClient.checkBatchStock(inventoryRequests);
            
            // Validate final stock
            for (InventoryValidationResponse stockResponse : finalStockCheck) {
                if (!stockResponse.isInStock()) {
                    validationErrors.add("Final stock check failed for " + stockResponse.getSkuCode() + ": " + stockResponse.getErrorMessage());
                }
            }
            
            if (!validationErrors.isEmpty()) {
                throw new CheckoutValidationException("Final inventory validation failed", validationErrors);
            }
            
            // STEP 4: Create Order
            log.info("Step 4: Creating order for user {}", checkoutRequest.getUserId());
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setClientId(checkoutRequest.getUserId());
            order.setTotalAmount(cartValidation.getTotalAmount());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setDeliveryAddress(checkoutRequest.getDeliveryAddress());
            order.setPaymentMethod(checkoutRequest.getPaymentMethod());
            order.setCreatedAt(LocalDateTime.now());

            // Create order lines from cart items
            List<OrderLine> orderLines = cartValidation.getItems().stream()
                    .map(cartItem -> {
                        OrderLine orderLine = new OrderLine();
                        orderLine.setSkuCode(cartItem.getSkuCode());
                        orderLine.setPrice(cartItem.getPrice());
                        orderLine.setQuantity(cartItem.getQuantity());
                        return orderLine;
                    })
                    .collect(Collectors.toList());
            
            order.setOrderLines(orderLines);
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            log.info("Order created successfully with ID: {}, Order Number: {}", savedOrder.getId(), savedOrder.getOrderNumber());
            // STEP 5: Deduct stock from inventory (NOUVELLE ÉTAPE)
            // ============================================================
            log.info("Step 5: Deducting stock from inventory service");
            try {
                inventoryClient.deductStock(inventoryRequests);
                log.info("Stock deducted successfully for order: {}", savedOrder.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to deduct stock for order {}: {}",
                        savedOrder.getOrderNumber(), e.getMessage());

                // Compensation: annuler la commande si la déduction échoue
                savedOrder.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(savedOrder);

                throw new RuntimeException("Order cancelled: Failed to update inventory - " + e.getMessage(), e);
            }
            // STEP 5: Clear cart after successful order creation
            log.info("Step 5: Clearing cart for user {}", checkoutRequest.getUserId());
            cartClient.clearCart(checkoutRequest.getUserId());
            
            log.info("Checkout completed successfully for user: {}, Order: {}", 
                    checkoutRequest.getUserId(), savedOrder.getOrderNumber());
            
            return mapper.toOrderResponse(savedOrder);
            
        } catch (ClientValidationException | CartEmptyException | CheckoutValidationException e) {
            log.error("Checkout validation failed for user {}: {}", checkoutRequest.getUserId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during checkout for user {}: {}", checkoutRequest.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Checkout failed due to unexpected error: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<OrderResponse> getAllOrders() {
        log.info("Retrieving all orders");
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(mapper::toOrderResponse)
                .collect(Collectors.toList());
    }
}
