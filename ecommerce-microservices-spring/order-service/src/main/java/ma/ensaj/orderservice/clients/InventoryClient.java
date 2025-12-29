package ma.ensaj.orderservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.orderservice.dtos.BatchInventoryRequest;
import ma.ensaj.orderservice.dtos.InventoryRequest;
import ma.ensaj.orderservice.dtos.InventoryResponse;
import ma.ensaj.orderservice.dtos.InventoryValidationResponse;
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

    /**
     * Vérifie la disponibilité du stock pour plusieurs produits
     */
    public List<InventoryValidationResponse> checkBatchStock(List<BatchInventoryRequest> requests) {
        try {
            log.info("Final stock validation for {} items before order creation", requests.size());

            return inventoryWebClient.post()
                    .uri("/api/inventory/check-batch")
                    .bodyValue(requests)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<InventoryValidationResponse>>() {})
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error during final stock validation: {}", e.getMessage());
            throw new RuntimeException("Failed final stock validation: " + e.getMessage());
        }
    }

    /**
     * Déduit le stock pour plusieurs produits (appel après création de commande)
     */
    public void deductStock(List<BatchInventoryRequest> requests) {
        try {
            log.info("Deducting stock for {} items", requests.size());

            inventoryWebClient.post()
                    .uri("/api/inventory/deduct")
                    .bodyValue(requests)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Stock deduction completed successfully");

        } catch (WebClientResponseException e) {
            log.error("Error during stock deduction: Status={}, Response={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to deduct stock: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during stock deduction: {}", e.getMessage());
            throw new RuntimeException("Failed to deduct stock: " + e.getMessage());
        }
    }

    /**
     * Vérifie le stock d'un seul produit
     */
    public InventoryResponse checkStock(String skuCode) {
        try {
            log.info("Checking stock for product: {}", skuCode);

            return inventoryWebClient.get()
                    .uri("/api/inventory/{skuCode}", skuCode)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error checking stock for {}: {}", skuCode, e.getMessage());
            throw new RuntimeException("Failed to check stock: " + e.getMessage());
        }
    }

    /**
     * Met à jour le stock d'un produit (méthode ancienne, préférer deductStock)
     */
    public InventoryResponse updateStock(InventoryRequest request) {
        try {
            log.info("Updating stock for product: {}", request.getSkuCode());

            return inventoryWebClient.put()
                    .uri("/api/inventory")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(InventoryResponse.class)
                    .block();

        } catch (WebClientResponseException e) {
            log.error("Error updating stock: {}", e.getMessage());
            throw new RuntimeException("Failed to update stock: " + e.getMessage());
        }
    }
}