package ma.ensaj.inventoryservice.dto;

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
    private boolean inStock;
    private Integer availableQuantity;
    private Integer requestedQuantity;
    private String errorMessage;
}