package ma.ensaj.cartservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.cartservice.clients.InventoryClient;
import ma.ensaj.cartservice.clients.ProductClient;
import ma.ensaj.cartservice.dtos.*;
import ma.ensaj.cartservice.exceptions.ProductValidationException;
import ma.ensaj.cartservice.exceptions.StockValidationException;
import ma.ensaj.cartservice.entities.Cart;
import ma.ensaj.cartservice.entities.CartItem;
import ma.ensaj.cartservice.mapper.CartMapper;
import ma.ensaj.cartservice.repositories.CartRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Override
    public CartResponse getCartByUserId(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = findOrCreateCart(userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, skuCode: {}", userId, request.getSkuCode());
        
        // Get price from product service since it's not provided in request anymore
        ProductValidationResponse productValidation = productClient.validateProduct(request.getSkuCode());
        if (!productValidation.getExists()) {
            throw new ProductValidationException("Product validation failed: " + productValidation.getErrorMessage());
        }

        Cart cart = findOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSkuCode().equals(request.getSkuCode()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity and use current validated price
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setPrice(productValidation.getPrice()); // Use current validated price
        } else {
            // Add new item with validated price
            CartItem newItem = cartMapper.toCartItem(request, productValidation.getPrice());
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        log.info("Item added successfully to cart for user: {} with validated price: {}", userId, productValidation.getPrice());
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse updateCartItem(Long userId, String skuCode, UpdateCartItemRequest request) {
        log.info("Updating cart item for user: {}, skuCode: {}", userId, skuCode);

        Cart cart = findCartByUserId(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getSkuCode().equals(skuCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart: " + skuCode));

        item.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart item updated successfully for user: {}", userId);

        return cartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse removeItemFromCart(Long userId, String skuCode) {
        log.info("Removing item from cart for user: {}, skuCode: {}", userId, skuCode);

        Cart cart = findCartByUserId(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getSkuCode().equals(skuCode));

        if (!removed) {
            throw new RuntimeException("Item not found in cart: " + skuCode);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        log.info("Item removed successfully from cart for user: {}", userId);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = findCartByUserId(userId);
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);
        log.info("Cart cleared successfully for user: {}", userId);
    }

    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    private Cart findCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }
    
    @Override
    public CartResponse addItemToCartWithValidation(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart with validation for user: {}, skuCode: {}", userId, request.getSkuCode());
        
        // Step 1: Validate product exists and get price
        ProductValidationResponse productValidation = productClient.validateProduct(request.getSkuCode());
        if (!productValidation.getExists()) {
            throw new ProductValidationException("Product validation failed: " + productValidation.getErrorMessage());
        }
        
        // Step 2: Check stock availability
        InventoryValidationResponse stockCheck = inventoryClient.checkStock(request.getSkuCode(), request.getQuantity());
        if (!stockCheck.getInStock()) {
            throw new StockValidationException("Stock validation failed: " + stockCheck.getErrorMessage());
        }
        
        // Step 3: Add to cart with validated price from product-service
        Cart cart = findOrCreateCart(userId);
        
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSkuCode().equals(request.getSkuCode()))
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity and price (in case price changed)
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setPrice(productValidation.getPrice()); // Use current validated price
        } else {
            // Add new item with validated price
            CartItem newItem = new CartItem();
            newItem.setSkuCode(request.getSkuCode());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(productValidation.getPrice()); // Use validated price from product-service
            cart.getItems().add(newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);
        
        log.info("Item added successfully to cart for user: {} with validated price: {}", userId, productValidation.getPrice());
        return cartMapper.toResponse(savedCart);
    }
    
    @Override
    public CartValidationResponse getValidatedCart(Long userId) {
        log.info("Getting validated cart for user: {}", userId);
        
        Cart cart = findCartByUserId(userId);
        
        if (cart.getItems().isEmpty()) {
            return CartValidationResponse.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .isValid(true)
                    .validationErrors(new ArrayList<>())
                    .build();
        }
        
        // Validate all products
        List<String> skuCodes = cart.getItems().stream()
                .map(CartItem::getSkuCode)
                .collect(Collectors.toList());
        
        List<ProductValidationResponse> productValidations = productClient.validateProducts(skuCodes);
        
        // Check stock for all items
        List<BatchInventoryRequest> inventoryRequests = cart.getItems().stream()
                .map(item -> new BatchInventoryRequest(item.getSkuCode(), item.getQuantity()))
                .collect(Collectors.toList());
        
        List<InventoryValidationResponse> stockValidations = inventoryClient.checkBatchStock(inventoryRequests);
        
        // Build response
        List<CartItemDto> validatedItems = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean isValid = true;
        
        for (CartItem item : cart.getItems()) {
            // Find product validation
            ProductValidationResponse productValidation = productValidations.stream()
                    .filter(pv -> pv.getSkuCode().equals(item.getSkuCode()))
                    .findFirst().orElse(null);
            
            // Find stock validation
            InventoryValidationResponse stockValidation = stockValidations.stream()
                    .filter(sv -> sv.getSkuCode().equals(item.getSkuCode()))
                    .findFirst().orElse(null);
            
            if (productValidation == null || !productValidation.getExists()) {
                validationErrors.add("Product not found: " + item.getSkuCode());
                isValid = false;
            } else if (stockValidation == null || !stockValidation.getInStock()) {
                validationErrors.add("Insufficient stock for: " + item.getSkuCode());
                isValid = false;
            } else {
                // Item is valid
                CartItemDto validatedItem = CartItemDto.builder()
                        .skuCode(item.getSkuCode())
                        .quantity(item.getQuantity())
                        .price(productValidation.getPrice())
                        .productName(productValidation.getProductName())
                        .build();
                
                validatedItems.add(validatedItem);
                totalAmount = totalAmount.add(productValidation.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        
        return CartValidationResponse.builder()
                .userId(userId)
                .items(validatedItems)
                .totalAmount(totalAmount)
                .isValid(isValid)
                .validationErrors(validationErrors)
                .build();
    }
}
