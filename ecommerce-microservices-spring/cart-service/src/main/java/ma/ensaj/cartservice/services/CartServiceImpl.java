package ma.ensaj.cartservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.cartservice.dtos.AddToCartRequest;
import ma.ensaj.cartservice.dtos.CartResponse;
import ma.ensaj.cartservice.dtos.UpdateCartItemRequest;
import ma.ensaj.cartservice.entities.Cart;
import ma.ensaj.cartservice.entities.CartItem;
import ma.ensaj.cartservice.mapper.CartMapper;
import ma.ensaj.cartservice.repositories.CartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCartByUserId(Long userId) {
        log.info("Fetching cart for user: {}", userId);
        Cart cart = findOrCreateCart(userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    public CartResponse addItemToCart(Long userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, skuCode: {}", userId, request.getSkuCode());

        Cart cart = findOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getSkuCode().equals(request.getSkuCode()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Mettre à jour la quantité si l'article existe déjà
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            item.setPrice(request.getPrice()); // Mettre à jour le prix
        } else {
            // Ajouter un nouvel article
            CartItem newItem = cartMapper.toCartItem(request);
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        log.info("Item added successfully to cart for user: {}", userId);
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
}
