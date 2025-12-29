package ma.ensaj.cartservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.cartservice.dtos.ProductValidationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {
    
    @Qualifier("productWebClient")
    private final WebClient productWebClient;
    
    public List<ProductValidationResponse> validateProducts(List<String> skuCodes) {
        try {
            log.info("Validating products with SKU codes: {}", skuCodes);
            
            return productWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/validate")
                            .queryParam("skuCodes", skuCodes)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ProductValidationResponse>>() {})
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error validating products: {}", e.getMessage());
            throw new RuntimeException("Failed to validate products: " + e.getMessage());
        }
    }
    
    public ProductValidationResponse validateProduct(String skuCode) {
        try {
            log.info("Validating product with SKU code: {}", skuCode);
            
            return productWebClient.get()
                    .uri("/api/products/validate/{skuCode}", skuCode)
                    .retrieve()
                    .bodyToMono(ProductValidationResponse.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error validating product {}: {}", skuCode, e.getMessage());
            throw new RuntimeException("Failed to validate product: " + e.getMessage());
        }
    }
}