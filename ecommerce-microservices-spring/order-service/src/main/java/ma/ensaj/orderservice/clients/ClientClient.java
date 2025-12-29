package ma.ensaj.orderservice.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensaj.orderservice.dtos.ClientValidationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientClient {
    
    @Qualifier("clientWebClient")
    private final WebClient clientWebClient;
    
    public ClientValidationResponse validateClient(Long clientId) {
        try {
            log.info("Validating client: {}", clientId);
            
            return clientWebClient.get()
                    .uri("/api/clients/validate/{clientId}", clientId)
                    .retrieve()
                    .bodyToMono(ClientValidationResponse.class)
                    .block();
                    
        } catch (WebClientResponseException e) {
            log.error("Error validating client {}: {}", clientId, e.getMessage());
            throw new RuntimeException("Failed to validate client: " + e.getMessage());
        }
    }
}