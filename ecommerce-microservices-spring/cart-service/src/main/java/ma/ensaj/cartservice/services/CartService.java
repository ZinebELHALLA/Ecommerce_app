package ma.ensaj.cartservice.services;

import ma.ensaj.cartservice.dtos.AddToCartRequest;
import ma.ensaj.cartservice.dtos.CartResponse;
import ma.ensaj.cartservice.dtos.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCartByUserId(Long userId);
    CartResponse addItemToCart(Long userId, AddToCartRequest request);
    CartResponse updateCartItem(Long userId, String skuCode, UpdateCartItemRequest request);
    CartResponse removeItemFromCart(Long userId, String skuCode);
    void clearCart(Long userId);
}
