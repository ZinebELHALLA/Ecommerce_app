package ma.ensaj.orderservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.orderservice.dtos.CartValidationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartClient {
    
    @Qualifier("cartWebClient")
    private final WebClient cartWebClient;
    
    public CartValidationResponse getValidatedCart(Long userId) {
        try {
            log.info("Getting validated cart for user: {}", userId);
            
            return cartWebClient.get()
                    .uri("/api/carts/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(CartValidationResponse.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error getting validated cart for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get cart validation: " + e.getMessage());
        }
    }
    
    public void clearCart(Long userId) {
        try {
            log.info("Clearing cart for user: {}", userId);
            
            cartWebClient.delete()
                    .uri("/api/carts/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to clear cart: " + e.getMessage());
        }
    }
}