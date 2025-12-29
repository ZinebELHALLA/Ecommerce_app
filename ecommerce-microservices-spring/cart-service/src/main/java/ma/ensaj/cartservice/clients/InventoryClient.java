package ma.ensaj.cartservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.cartservice.dtos.BatchInventoryRequest;
import ma.ensaj.cartservice.dtos.InventoryValidationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {
    
    @Qualifier("inventoryWebClient")
    private final WebClient inventoryWebClient;
    
    public List<InventoryValidationResponse> checkBatchStock(List<BatchInventoryRequest> requests) {
        try {
            log.info("Checking batch stock for {} items", requests.size());
            
            return inventoryWebClient.post()
                    .uri("/api/inventory/check-batch")
                    .bodyValue(requests)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InventoryValidationResponse>>() {})
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error checking batch stock: {}", e.getMessage());
            throw new RuntimeException("Failed to check stock availability: " + e.getMessage());
        }
    }
    
    public InventoryValidationResponse checkStock(String skuCode, Integer quantity) {
        try {
            log.info("Checking stock for SKU: {} with quantity: {}", skuCode, quantity);
            
            return inventoryWebClient.get()
                    .uri("/api/inventory/check/{skuCode}?quantity={quantity}", skuCode, quantity)
                    .retrieve()
                    .bodyToMono(InventoryValidationResponse.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error checking stock for {}: {}", skuCode, e.getMessage());
            throw new RuntimeException("Failed to check stock availability: " + e.getMessage());
        }
    }
}