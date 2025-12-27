package ma.ensaj.orderservice.dtos;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderLineRequest {
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
}
