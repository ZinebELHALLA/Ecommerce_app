package ma.ensaj.inventoryservice.dto;

import lombok.Data;

@Data

public class InventoryResponse {
    private String skuCode;
    private Integer quantity;
    private boolean inStock;
}
