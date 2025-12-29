package ma.ensaj.orderservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchInventoryRequest {
    private String skuCode;
    private Integer requiredQuantity;
}