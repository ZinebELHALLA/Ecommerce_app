package ma.ensaj.cartservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductValidationResponse {
    private String skuCode;
    private Boolean exists;
    private String productName;
    private BigDecimal price;
    private String errorMessage;
}