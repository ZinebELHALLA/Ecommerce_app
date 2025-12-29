package ma.ensaj.cartservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryValidationResponse {
    private String skuCode;
    private Boolean inStock;
    private Integer availableQuantity;
    private Integer requestedQuantity;
    private String errorMessage;
}