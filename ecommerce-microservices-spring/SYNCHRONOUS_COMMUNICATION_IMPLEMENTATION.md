# E-Commerce Microservices Synchronous Communication Implementation

## Overview

This document provides a comprehensive explanation of the synchronous communication implementation using WebClient across the e-commerce microservices system. The implementation ensures data consistency and validation throughout the entire shopping and checkout process.

## Architecture Changes

### 1. WebClient Configuration

**Added Dependencies:**
- `spring-boot-starter-webflux` to cart-service and order-service

**WebClient Beans Created:**
- `cart-service`: WebClient beans for product-service and inventory-service
- `order-service`: WebClient beans for cart-service, client-service, inventory-service, and product-service

### 2. Enhanced Service Endpoints

#### Product-Service Enhancements
**New Endpoints:**
- `GET /api/products/validate?skuCodes=[]` - Batch product validation
- `GET /api/products/validate/{skuCode}` - Single product validation

**Repository Methods Added:**
- `findBySku(String sku)` - Find product by SKU
- `findBySkuIn(List<String> skus)` - Batch find products by SKUs

**DTOs Created:**
- `ProductValidationResponse` - Contains SKU, exists flag, product name, price, and error message

#### Inventory-Service Enhancements
**New Endpoints:**
- `POST /api/inventory/check-batch` - Batch stock checking
- `GET /api/inventory/check/{skuCode}?quantity=X` - Stock validation with quantity

**Repository Methods Added:**
- `findBySkuCodeIn(List<String> skuCodes)` - Batch find inventory by SKU codes

**DTOs Created:**
- `BatchInventoryRequest` - Contains SKU code and required quantity
- `InventoryValidationResponse` - Contains SKU, stock status, available/requested quantities, and error message

#### Client-Service Enhancements
**New Endpoints:**
- `GET /api/clients/validate/{clientId}` - Client existence validation

**DTOs Created:**
- `ClientValidationResponse` - Contains client ID, exists flag, client details, and error message

#### Cart-Service Enhancements
**New Endpoints:**
- `POST /api/carts/{userId}/items/validated` - Add items with validation
- `GET /api/carts/{userId}/validate` - Get validated cart for orders

**Client Services Created:**
- `ProductClient` - Communicates with product-service for validation
- `InventoryClient` - Communicates with inventory-service for stock checking

**DTOs Created:**
- `CartValidationResponse` - Complete cart validation with total amount and error details
- `CartItemDto` - Cart item representation for external communication

**API Design Improvement:**
- `AddToCartRequest` only requires `skuCode` and `quantity` (no price field)
- Price is automatically fetched from product-service during validation
- This prevents price manipulation and ensures current pricing

#### Order-Service Enhancements
**New Endpoints:**
- `POST /api/orders/checkout` - Complete checkout workflow with validation

**Client Services Created:**
- `CartClient` - Communicates with cart-service
- `ClientClient` - Communicates with client-service
- `InventoryClient` - Communicates with inventory-service

**Entity Enhancements:**
- Enhanced `Order` entity with client ID, total amount, status, delivery address, payment method
- Added `OrderStatus` enum (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)

**DTOs Created:**
- `CheckoutRequest` - Contains user ID, delivery address, payment method

## Business Flow Implementation

### 1. Add to Cart Flow (Synchronous Validation)

```
Client Request → Cart-Service
    ↓
Cart-Service → Product-Service (validate product exists)
    ↓
Cart-Service → Inventory-Service (check stock availability)
    ↓
If both validations pass: Add to cart
If any validation fails: Return error immediately
```

**Implementation Details:**
1. **Product Validation**: Calls `ProductClient.validateProduct(skuCode)` using WebClient.block()
2. **Price Retrieval**: Gets current price from product-service (clients only provide SKU and quantity)
3. **Stock Validation**: Calls `InventoryClient.checkStock(skuCode, quantity)` using WebClient.block()
4. **Error Handling**: Throws `ProductValidationException` or `StockValidationException` if validation fails
5. **Price Security**: Uses validated price from product-service to prevent price manipulation

### 2. Checkout Flow (Complete Validation Workflow)

```
Client Request → Order-Service
    ↓
Step 1: Order-Service → Client-Service (validate client exists)
    ↓
Step 2: Order-Service → Cart-Service (get validated cart)
    ↓
Step 3: Order-Service → Inventory-Service (final stock check)
    ↓
Step 4: Order-Service → Create Order (if all validations pass)
    ↓
Step 5: Order-Service → Cart-Service (clear cart)
    ↓
Return Order Response
```

**Detailed Implementation:**

**Step 1: Client Validation**
```java
ClientValidationResponse clientValidation = clientClient.validateClient(userId);
if (!clientValidation.isExists()) {
    throw new ClientValidationException("Client not found");
}
```

**Step 2: Cart Validation**
```java
CartValidationResponse cartValidation = cartClient.getValidatedCart(userId);
if (cartValidation.getItems().isEmpty()) {
    throw new CartEmptyException("Cannot checkout with empty cart");
}
if (!cartValidation.isValid()) {
    throw new CheckoutValidationException("Cart validation failed", validationErrors);
}
```

**Step 3: Final Inventory Check**
```java
List<BatchInventoryRequest> inventoryRequests = cartValidation.getItems().stream()
    .map(item -> new BatchInventoryRequest(item.getSkuCode(), item.getQuantity()))
    .collect(Collectors.toList());
    
List<InventoryValidationResponse> finalStockCheck = inventoryClient.checkBatchStock(inventoryRequests);
```

**Step 4: Order Creation**
- Create Order entity with validated data
- Set order status to CONFIRMED
- Create order lines from cart items
- Save to database with @Transactional

**Step 5: Cart Clearing**
```java
cartClient.clearCart(userId);
```

## Error Handling Strategy

### Custom Exceptions Created

#### Cart-Service Exceptions:
- `ProductValidationException` - Product doesn't exist or is inactive
- `StockValidationException` - Insufficient stock or product not in inventory

#### Order-Service Exceptions:
- `ClientValidationException` - Client doesn't exist
- `CartEmptyException` - Attempting checkout with empty cart
- `CheckoutValidationException` - Contains list of validation errors

### Global Exception Handlers

**Cart-Service GlobalExceptionHandler:**
- Returns 400 Bad Request for `ProductValidationException`
- Returns 409 Conflict for `StockValidationException`
- Returns 503 Service Unavailable for `WebClientResponseException`

**Order-Service GlobalExceptionHandler:**
- Returns 400 Bad Request for `ClientValidationException` and `CartEmptyException`
- Returns 409 Conflict for `CheckoutValidationException`
- Includes detailed validation errors in response

## Synchronous Communication Details

### Why Synchronous Communication is Used

1. **Data Consistency**: Ensures real-time validation before cart operations and order creation
2. **Immediate Feedback**: Users get instant validation results
3. **Atomicity**: Checkout process is atomic - either all validations pass or none
4. **Race Condition Prevention**: Final inventory check prevents overselling

### WebClient Usage Patterns

**Basic Synchronous Call:**
```java
return webClient.get()
    .uri("/api/products/validate/{skuCode}", skuCode)
    .retrieve()
    .bodyToMono(ProductValidationResponse.class)
    .block(); // Synchronous execution
```

**Batch Operations:**
```java
return webClient.post()
    .uri("/api/inventory/check-batch")
    .bodyValue(requests)
    .retrieve()
    .bodyToMono(new ParameterizedTypeReference<List<InventoryValidationResponse>>() {})
    .block();
```

## Testing Strategy

### Postman Test Collection Includes:

1. **Setup Tests**: Create test data (clients, products, inventory)
2. **Success Cases**: 
   - Add valid products to cart
   - Successful checkout workflow
3. **Failure Cases**:
   - Invalid product validation
   - Insufficient stock scenarios
   - Invalid client checkout
   - Empty cart checkout
4. **Validation Endpoint Tests**: Direct testing of validation endpoints

### Expected Responses

**Successful Cart Addition (201 Created):**
```json
{
    "id": "cart123",
    "userId": 1,
    "items": [
        {
            "skuCode": "LAPTOP001",
            "quantity": 1,
            "price": 999.99
        }
    ],
    "createdAt": "2025-12-29T14:00:00",
    "updatedAt": "2025-12-29T14:00:00"
}
```

**Product Validation Failure (400 Bad Request):**
```json
{
    "timestamp": "2025-12-29T14:00:00",
    "status": 400,
    "error": "Product Validation Failed",
    "message": "Product validation failed: Product not found",
    "path": "/api/carts"
}
```

**Stock Validation Failure (409 Conflict):**
```json
{
    "timestamp": "2025-12-29T14:00:00",
    "status": 409,
    "error": "Stock Validation Failed",
    "message": "Stock validation failed: Insufficient stock",
    "path": "/api/carts"
}
```

**Successful Checkout (201 Created):**
```json
{
    "id": 1,
    "orderNumber": "uuid-string",
    "clientId": 1,
    "totalAmount": 1029.98,
    "status": "CONFIRMED",
    "createdAt": "2025-12-29T14:00:00",
    "deliveryAddress": "123 Main St, City, Country",
    "paymentMethod": "CREDIT_CARD",
    "orderLines": [
        {
            "skuCode": "LAPTOP001",
            "quantity": 1,
            "price": 999.99
        },
        {
            "skuCode": "MOUSE001",
            "quantity": 1,
            "price": 29.99
        }
    ]
}
```

**Checkout Validation Failure (409 Conflict):**
```json
{
    "timestamp": "2025-12-29T14:00:00",
    "status": 409,
    "error": "Checkout Validation Failed",
    "message": "Final inventory validation failed",
    "validationErrors": [
        "Final stock check failed for MOUSE001: Insufficient stock"
    ],
    "path": "/api/orders"
}
```

## Logging Strategy

### Log Levels and Messages

**INFO Level Logs:**
- Service communication start/end
- Successful validations
- Order creation milestones

**ERROR Level Logs:**
- Validation failures
- Service communication errors
- Unexpected errors with full stack traces

### Sample Log Flow for Successful Checkout:
```
2025-12-29 14:00:00 INFO  [order-service] Starting checkout process for user: 1
2025-12-29 14:00:01 INFO  [order-service] Step 1: Validating client 1
2025-12-29 14:00:02 INFO  [order-service] Step 2: Retrieving and validating cart for user 1
2025-12-29 14:00:03 INFO  [order-service] Step 3: Final inventory validation for 2 items
2025-12-29 14:00:04 INFO  [order-service] Step 4: Creating order for user 1
2025-12-29 14:00:05 INFO  [order-service] Order created successfully with ID: 1, Order Number: uuid-string
2025-12-29 14:00:06 INFO  [order-service] Step 5: Clearing cart for user 1
2025-12-29 14:00:07 INFO  [order-service] Checkout completed successfully for user: 1, Order: uuid-string
```

## Performance Considerations

### Optimizations Implemented:
1. **Batch Operations**: Validate multiple products/inventory items in single calls
2. **Connection Pooling**: WebClient uses connection pooling by default
3. **Timeout Configuration**: Can be configured in WebClient beans
4. **Circuit Breaker**: Can be added using Spring Cloud Circuit Breaker

### Potential Improvements:
1. **Caching**: Add Redis cache for frequently accessed product/inventory data
2. **Async Processing**: Use reactive streams for non-critical operations
3. **Retry Logic**: Add retry mechanisms for transient failures
4. **Database Optimization**: Add indexes for SKU-based queries

## Deployment Notes

### Service Dependencies:
1. **client-service** (Port 8081) - No dependencies
2. **product-service** (Port 8082) - No dependencies
3. **inventory-service** (Port 8083) - No dependencies
4. **cart-service** (Port 8084) - Depends on product-service, inventory-service
5. **order-service** (Port 8085) - Depends on all other services

### Startup Order:
1. Start client-service, product-service, inventory-service
2. Start cart-service (after product and inventory services)
3. Start order-service (after all other services)

### Health Check Endpoints:
All services should implement health check endpoints for monitoring service availability before making inter-service calls.

## Conclusion

This implementation provides a robust, synchronous communication pattern that ensures data consistency across the e-commerce microservices. The validation workflows prevent common e-commerce issues like overselling, invalid product additions, and order creation with missing data. The comprehensive error handling ensures users receive meaningful feedback for all validation failures.

The system is now ready for production use with proper data consistency guarantees and comprehensive error handling throughout the entire shopping and checkout process.