package ma.ensaj.cartservice.mapper;

import ma.ensaj.cartservice.dtos.*;
import ma.ensaj.cartservice.entities.Cart;
import ma.ensaj.cartservice.entities.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    public CartResponse toResponse(Cart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponse> itemResponses = cart.getItems() != null
                ? cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = itemResponses.stream()
                .map(CartItemResponse::getQuantity)
                .reduce(0, Integer::sum);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .totalItems(totalItems)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null) {
            return null;
        }

        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .skuCode(item.getSkuCode())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }

    public CartItem toCartItem(AddToCartRequest request) {
        if (request == null) {
            return null;
        }

        return new CartItem(
                request.getSkuCode(),
                request.getQuantity(),
                BigDecimal.ZERO // Price will be set later from product validation
        );
    }
    
    public CartItem toCartItem(AddToCartRequest request, BigDecimal validatedPrice) {
        if (request == null) {
            return null;
        }

        return new CartItem(
                request.getSkuCode(),
                request.getQuantity(),
                validatedPrice
        );
    }
}
