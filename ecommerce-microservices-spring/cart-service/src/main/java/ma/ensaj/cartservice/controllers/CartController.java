package ma.ensaj.cartservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.cartservice.dtos.AddToCartRequest;
import ma.ensaj.cartservice.dtos.CartResponse;
import ma.ensaj.cartservice.dtos.UpdateCartItemRequest;
import ma.ensaj.cartservice.services.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        log.info("GET request to fetch cart for user: {}", userId);
        CartResponse cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        log.info("POST request to add item to cart for user: {}", userId);
        CartResponse cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PutMapping("/{userId}/items/{skuCode}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long userId,
            @PathVariable String skuCode,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT request to update cart item for user: {}, skuCode: {}", userId, skuCode);
        CartResponse cart = cartService.updateCartItem(userId, skuCode, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}/items/{skuCode}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable String skuCode) {
        log.info("DELETE request to remove item from cart for user: {}, skuCode: {}", userId, skuCode);
        CartResponse cart = cartService.removeItemFromCart(userId, skuCode);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        log.info("DELETE request to clear cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}