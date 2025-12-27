package ma.ensaj.cartservice.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String skuCode;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
